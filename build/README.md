# Customer Use Instructions

This contains the information needed to start the server and access the application, for additional resources see the `README.md` located in the [project root](https://github.com/GameKhan13/Catalog-Project.git)

## Requirements
### Java 17
Check for a java instalation using
```bash
java --version
```
If your version is lower than 17 follow the guide [here](https://docs.oracle.com/en/java/javase/17/install/index.html#GUID-A0EAF7DB-67EB-4218-AD54-3E54CDD8D89E) to install java

## Run Instructions

__**Windows**__ <br>
1. Find the file `run_windows.cmd`, this should be found in the same directory as this file.
2. Double click to run
3. A commandpromt window should open, this means the server is running
4. go to http://localhost:4570/ to find the web project

__**Mac & Linux**__ <br>
1. Find the file `run_unix.sh`, this should be found in the same directory as this file.
2. Double click or type `./run_unix.sh`
3. The project will open/run on a terminal
4. go to http://localhost:4570/ to find the web project

## Troubleshooting
* **Errors Starting Application**
    * Check for java installation run: `java --version`
    * tutorial to download found [here](https://docs.oracle.com/en/java/javase/17/install/index.html#GUID-A0EAF7DB-67EB-4218-AD54-3E54CDD8D89E)
* **File Not Found Error**
    * Ensure the file you are executing is located in the same folder as `musiccatalog-1.0.0-jar-with-dependencies.jar`
    * If you want the executable in another location create a shortcut
* **Permission Denied**
    * For Unix & Mac run: `chmod +x run_unix.sh` in the terminal