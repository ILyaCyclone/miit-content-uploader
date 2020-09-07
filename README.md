Mass file uploader for MIIT content schema with command-line interface

Requires Java 8 or higher to run.

*Remember the sad DB limitation of supported files formats.*

# Usage

Uploader following commands:
- `upload-folder` to upload a single folder
- `upload-folder-structure` to upload a folder structure
- `-help` print short help
- `-version` print program version

## upload single folder of files
call arguments: `upload-folder D:\\content-mass-upload\\images 169657 1162`
- `D:\\content-mass-upload\\images` - a folder, containing files for uploading;
- `169657` - id_information whereto files will be uploaded (information must be created beforehand);
- `1162` - idk_content for created contents

`D:\content-mass-upload\images.csv` file will be created as output.

## upload folder structure
call arguments: `upload-folder-structure D:\\content-mass-upload\\images-root 1162`
- `D:\\content-mass-upload\\images-root` - a folder, containing subfolders, containing files for uploading.
  Each subfolder name is considered to id_information whereto files will be uploaded (all informations must be created beforehand);
- `1162` - idk_content for created contents

`D:\content-mass-upload\images-root_csv` folder will be created as output.

# Configuration

- `uploader.db.username` key or `UPLOADER_DB_USERNAME` env variable  
Username to connect to database  
**required**

- `uploader.db.password` key or `UPLOADER_DB_PASSWORD` env variable  
Password to connect to database  
**required**

- `uploader.id-lang` key or `UPLOADER_IDLANG` env variable  
ID language for created content  
Valid values: 1 - Russian, 2 - English, 3 - German  
Default value: 1 

- `uploader.query-binary-metadata` key or `UPLOADER.QUERYBINARYMETADATA` env variable  
Set this to true if you need metadata, such as width, height, d_last and hash in output.  
Valid values: true, false  
Default value: false (`true` requires separate DB query for each uploaded file, so `false` is somewhat faster)

- `uploader.dry-run` key or `UPLOADER.DRYRUN` env variable  
Dry run for testing, doesn't insert anything to database, but produces output close to real.  
Valid values: true, false  
Default value: false

# Examples

Example 1 - run keys only

`java -jar -Duploader.db.username=xxx -Duploader.db.password=zzz uploader.jar upload-folder D:\\content-mass-upload\\images 169657 1162`

Example 2 - with environment variables

`set UPLOADER_DB_USERNAME=xxx`  
`set UPLOADER_DB_PASSWORD=zzz`  
`java -jar -Duploader.query-binary-metadata=true -Duploader.id-lang=2 uploader.jar upload-folder-structure D:\\content-mass-upload\\images-root 1162`

Example 3 - folder structure

```
images-root/  
|-- 111/
|   |-- image1.jpg  
|   |-- image2.jpg  
|
|-- 112/
    |-- image3.jpg  
    |-- image4.jpg  
```

`111` and `112` must be IDs or existing informations.