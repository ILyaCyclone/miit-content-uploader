Mass file uploader for MIIT content schema.

Provide database user credentials as environment variables:
- DB_USERNAME=\<username>
- DB_PASSWORD=\<password>

Remember the sad limitation of supported files formats.

How to use:
# upload single folder of files
call arguments: `upload-folder D:\\content-mass-upload\\images 169657 1162`
- `D:\\content-mass-upload\\images` - a folder, containing files for uploading;
- `169657` - id_information whereto files will be uploaded (information must be created beforehand);
- `1162` - idk_content for created contents

`D:\content-mass-upload\images.csv` file will be created as output.

# upload a folder tree
call arguments: `upload-folder-tree D:\\content-mass-upload\\images-root 1162`
- `D:\\content-mass-upload\\images-root` - a folder, containing subfolders, containing files for uploading.
  Each subfolder name is considered to id_information whereto files will be uploaded (all informations must be created beforehand);
- `1162` - idk_content for created contents 

`D:\content-mass-upload\images-root_csv` folder will be created as output.