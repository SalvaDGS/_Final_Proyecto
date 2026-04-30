#MacOS:
#
#Abrir .dmg y copiar la aplicación en la carpeta aplicaciones como se indica.
#
#Al iniciar la app se crea una carpeta AVTech Invoice en el home donde se ubican la base de 
#datos y las carpeta para facturas y tokens.
#
#En algunos sistemas (especialmente Apple Silicon)puede ser necesario forzar los permisos con el 
#comando: codesign --force --deep --sign - /Applications/AVTech\ Invoice.app
#ya que la app no está firmada.
#
#Windows:
#
#Ejecutar el .exe.
#
#Al iniciar la app se crea una carpeta AVTech Invoice en 
#la carpeta del usuario (C:\Users\usuario) donde 
#se ubican la base de datos y las carpeta para facturas y tokens.
#
#Es posible que se requiera desactivar el control inteligente de 
#aplicaciones en seguridad de windows para instalar la app ya que no está firmada.