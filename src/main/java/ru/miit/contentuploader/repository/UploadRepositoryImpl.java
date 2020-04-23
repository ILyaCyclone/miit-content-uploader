package ru.miit.contentuploader.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.stereotype.Repository;
import ru.miit.contentuploader.config.UploaderProperties;
import ru.miit.contentuploader.model.Content;
import ru.miit.contentuploader.model.ContentVersion;
import ru.miit.contentuploader.model.LargeBinaryData;
import ru.miit.contentuploader.model.LargeBinaryMetadata;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Repository
@ConditionalOnProperty(name="uploader.dry-run", havingValue = "false", matchIfMissing = true)
public class UploadRepositoryImpl implements UploadRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UploaderProperties uploaderProperties;
    private final SimpleJdbcCall calcBlobAttributesCall;

    public UploadRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, UploaderProperties uploaderProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.uploaderProperties = uploaderProperties;

        calcBlobAttributesCall = new SimpleJdbcCall(this.jdbcTemplate.getJdbcTemplate())
                .withSchemaName("mwpr1")
                .withCatalogName("wpms_fp")
                .withFunctionName("ApplyCalcCVAttributes");
    }

    @Override
    public Content createContent(long idkContent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("insert into content_wp(idk_content, use_sign) values(:idk_content, 1)"
                , new MapSqlParameterSource("idk_content", idkContent)
                , keyHolder, new String[]{"id_content", "id_web_metaterm"});
        Content content = new Content();
        content.setIdContent(((BigDecimal) keyHolder.getKeys().get("id_content")).longValue());
        content.setIdWebMetaterm(((BigDecimal) keyHolder.getKeys().get("id_web_metaterm")).longValue());
        return content;
    }

    @Override
    @Cacheable
    public int getIdkFormat(String extension) {
        try {
            return jdbcTemplate.queryForObject("select e.idk_format_content from expansions_types_files_wp e where lower(e.extension) = lower(:extension)"
                    , new MapSqlParameterSource("extension", extension), Integer.class);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Could not find idkFormat for extension: " + extension, e);
        }
    }

    @Override
    public void createInformationContent(long idInfo, long idContent) {
        jdbcTemplate.update("insert into information_content_wp (id_information, id_content, use_sign) values (:id_information, :id_content, 1)"
                , new MapSqlParameterSource("id_information", idInfo).addValue("id_content", idContent));
    }

    public ContentVersion createContentVersion(long idContent, int idkFormat, int idLang) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("insert into content_version_wp(id_content, idk_format_content, id_content_languages, use_sign, idwm_as_name) " +
                        "values(:id_content, :idk_format_content, :id_content_languages, 1, 2)"
                , new MapSqlParameterSource()
                        .addValue("id_content", idContent).addValue("idk_format_content", idkFormat)
                        .addValue("id_content_languages", idLang)
                , keyHolder, new String[]{"id_content_version", "id_web_metaterm"});
        ContentVersion contentVersion = new ContentVersion();
        contentVersion.setIdContentVersion(((BigDecimal) keyHolder.getKeys().get("id_content_version")).longValue());
        contentVersion.setIdWebMetaterm(((BigDecimal) keyHolder.getKeys().get("id_web_metaterm")).longValue());
        contentVersion.setIdkFormat(idkFormat);
        contentVersion.setIdContent(idContent);
        contentVersion.setIdLang(idLang);
        return contentVersion;
    }

    @Override
    public LargeBinaryData createLargeBinaryData(long idContentVersion, String filename, InputStream inputStream, int length) {
        // Spring keyHolder can't be used to retrieve generated date from Oracle DB
        // so d_last is moved to LargeBinaryMetadata and can fetched with separate query
//        SqlParameterSource params = new MapSqlParameterSource()
//                .addValue("id_content_version", idContentVersion)
//                .addValue("filename", filename)
//                .addValue("data_binary", new SqlLobValue(inputStream, length, new DefaultLobHandler()), Types.BLOB);
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        jdbcTemplate.update("insert into large_binary_data_wp (id_content_version, filename, data_binary) values (:id_content_version, :filename, :data_binary)"
//                , params
//                , keyHolder, new String[]{"d_last"}
//                );

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_content_version", idContentVersion)
                .addValue("filename", filename)
                .addValue("data_binary", new SqlLobValue(inputStream, length, new DefaultLobHandler()), Types.BLOB);
        jdbcTemplate.update("insert into large_binary_data_wp (id_content_version, filename, data_binary) values (:id_content_version, :filename, :data_binary)"
                , params);

        calculateBlobAttributes(idContentVersion);

        LargeBinaryData largeBinaryData = new LargeBinaryData();
        largeBinaryData.setIdContentVersion(idContentVersion);
        largeBinaryData.setFilename(filename);
//        largeBinaryData.setLastModified((Instant)keyHolder.getKeys().get("d_last"));

        if (uploaderProperties.isQueryBinaryCalculatedAttributes()) {
            LargeBinaryMetadata metadata = getLargeBinaryMetadata(idContentVersion);
            largeBinaryData.setMetadata(metadata);
        }
        return largeBinaryData;
    }

    private LargeBinaryMetadata getLargeBinaryMetadata(long idContentVersion) {
        return jdbcTemplate.queryForObject("select d_last, hash_sh1, width, height from large_binary_data_wp where id_content_version = :id_content_version"
                , new MapSqlParameterSource("id_content_version", idContentVersion)
                , (rs, rowNum) -> new LargeBinaryMetadata(rs.getTimestamp("d_last").toInstant()
                        , rs.getString("hash_sh1")
                        , getIntegerFromResultSet(rs, "width")
                        , getIntegerFromResultSet(rs, "height"))
        );
    }

    private void calculateBlobAttributes(long idContentVersion) {
        calcBlobAttributesCall.executeFunction(BigDecimal.class, new MapSqlParameterSource()
                .addValue("Aid_content_version", idContentVersion)
                .addValue("A_is_commit", 0)
                .addValue("A_is_debug", 0));
    }


    private Integer getIntegerFromResultSet(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        return rs.wasNull() ? null : value;
    }
}
