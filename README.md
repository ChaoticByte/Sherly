# xxSherly

A fork of [Sherly](https://github.com/BlyDoesCoding/Sherly), using [xxHash](https://github.com/Cyan4973/xxHash).  
This fork is faster, but has less features and may produce false-positives.

![](./images/screenshot.png)

## Introduction

Sherly is a Multithreaded Duplicate File Finder for your Terminal, written in java. You can Easily find duplicate Images, videos as well as any other type of Data. That can be helpful if you run on small storage or just want to keep regular housekeeping.

Instead of md5, this fork uses [xxHash](https://github.com/Cyan4973/xxHash) + the filesize to find duplicates, for performance reasons (see [Speed comparison](#speed-comparison)).
Note that xxHash is not a cryptographic hash function and therefore may produce collisions (false-positives). For this reason, since version 2.1, the program no longer offers the option to delete duplicates. You should delete them by yourself.

## Usage

```
usage: xxSherly.jar [options] folder1 folder2 ...
 -c,--color           enable colored output
 -h,--help            show this help message
 -p,--progress        enable progress indicator
 -t,--threads <arg>   override default thread number (defaults to the
                      number of cores)
 -v,--verbose         more verbose output
```

## Build

```bash
mvn package assembly:single
```

## Supported Platforms

| OS                |    Working     | Version |
| ----------------- | :------------: | ------: |
| Linux             |      Yes       |     1.0 |
| Windows 10/11     | Not yet tested |       - |
| macOS             | Not yet tested |       - |
| BSD               | Not yet tested |       - |

## Speed comparison

I let Sherly and xxSherly find duplicates in my Music Library (containing `.wav` files) using the following commands:

```bash
time java -jar Bin/sherly.jar -n -f ~/Music/
time java -jar target/xxSherly-x.y-jar-with-dependencies.jar -n -f ~/Music/
```

The timings are measured using the Linux tool `time` (`real`).

|           | Sherly v1.1.4 |   xxSherly v1.0 |
| --------: | ------------: | --------------: |
|  1st run  |        4.055s |          2.561s |
|  2nd run  |        4.055s |          2.304s |
|  3rd run  |        4.066s |          2.549s |
|  **avg**  |    **4.059s** |      **2.471s** |
