# Darwin Runtimes

This directory contains the runtimes for the Darwin platform.

## Repo Structure

This folder is organized as follows:

    .
    ├── cpu
    │   ├── <RUNTIME>
    │   │   ├── Dockerfile
    ├── gpu
    │   ├── <RUNTIME>
    │   │   ├── Dockerfile


## Building

Runtimes are built from Dockerfiles.

## Adding a new runtime

To add a new runtime, create a new folder under the appropriate CPU or GPU folder. The folder name should be the name of the runtime. Add a Dockerfile to the folder.

## Documentation Generator

To generate the documentation, run the following commands in the cluster created using the Runtime on Darwin:
```
cd runtimes/
python3 runtime_doc_generator.py
```
