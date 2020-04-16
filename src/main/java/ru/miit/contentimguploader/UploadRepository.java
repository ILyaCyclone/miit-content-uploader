package ru.miit.contentimguploader;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Types;

@Repository
public class UploadRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall calcBlobAttributesCall;

    public UploadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        calcBlobAttributesCall = new SimpleJdbcCall(this.jdbcTemplate.getJdbcTemplate())
                .withSchemaName("mwpr1")
                .withCatalogName("wpms_fp")
                .withFunctionName("ApplyCalcCVAttributes");
    }

    public Long createContent(long idkContent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("insert into content_wp(idk_content, use_sign) values(:idk_content, 1)"
                , new MapSqlParameterSource("idk_content", idkContent)
                , keyHolder, new String[]{"id_content"});
        return keyHolder.getKey().longValue();
    }


    @Cacheable
    public long getIdkFormat(String extension) {
        try {
            return jdbcTemplate.queryForObject("select e.idk_format_content from expansions_types_files_wp e where lower(e.extension) = lower(:extension)"
                    , new MapSqlParameterSource("extension", extension), Long.class);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Could not find idkFormat for extension: " + extension, e);
        }
    }

    public void createInformationContent(long idInfo, long idContent) {
        jdbcTemplate.update("insert into information_content_wp (id_information, id_content, use_sign) values (:id_information, :id_content, 1)"
                , new MapSqlParameterSource("id_information", idInfo).addValue("id_content", idContent));
    }

    public long createContentVersion(long idContent, long idkFormat, long idLang) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("insert into content_version_wp(id_content, idk_format_content, id_content_languages, use_sign, idwm_as_name) " +
                        "values(:id_content, :idk_format_content, :id_content_languages, 1, 2)"
                , new MapSqlParameterSource()
                        .addValue("id_content", idContent).addValue("idk_format_content", idkFormat)
                        .addValue("id_content_languages", idLang)
                , keyHolder, new String[]{"id_content_version"});
        return keyHolder.getKey().longValue();
    }

    public void createLargeBinaryData(long idContentVersion, String filename, InputStream inputStream, int length) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_content_version", idContentVersion)
                .addValue("filename", filename)
                .addValue("data_binary", new SqlLobValue(inputStream, length, new DefaultLobHandler()), Types.BLOB);
        jdbcTemplate.update("insert into large_binary_data_wp (id_content_version, filename, data_binary) values(:id_content_version, :filename, :data_binary)"
                , params);

        calculateBlobAttributes(idContentVersion);
    }

    private void calculateBlobAttributes(long idContentVersion) {
        calcBlobAttributesCall.executeFunction(BigDecimal.class, new MapSqlParameterSource()
                .addValue("Aid_content_version", idContentVersion)
                .addValue("A_is_commit", 0)
                .addValue("A_is_debug", 0));
    }
}
