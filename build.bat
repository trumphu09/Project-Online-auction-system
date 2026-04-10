@echo off
echo === BUILDING AUCTION SYSTEM ===

echo Building Server...
cd Server
if exist target rmdir /s /q target
mkdir target\classes

REM Compile Server
javac -cp "lib/*" -d target\classes src\main\java\com\auction\server\*.java src\main\java\com\auction\server\models\*.java src\main\java\com\auction\server\dao\*.java src\main\java\com\auction\server\websocket\*.java

echo Building Client...
cd ..\Client
if exist target rmdir /s /q target
mkdir target\classes

REM Compile Client
javac --module-path "lib" --add-modules javafx.controls,javafx.fxml -cp "lib/*" -d target\classes src\main\java\org\example\*.java src\main\java\org\example\controller\*.java src\main\java\org\example\model\*.java

cd ..
echo Build completed!
pause