Comparing Ray-Tracer
====================

This is an implementation of a simple ray-tracer
with the added ability of visually comparing the
performance of different storage algorithms.

Getting Started
---------------

You can either import the project into eclipse and
run `cgp.Main` from there or you can use the existing
binaries (Mac and Linux only).

In order to use the binaries extract the contents
of `run.zip` to the destination of your liking and
execute `run.sh` from a shell.

How to use the application
--------------------------

In the menu of the application you can select the
model to load and the storage algorithm to use.
The application opens two windows, the navigation
view and the actual ray-tracing output.

When the navigation view has keyboard focus you can
move the camera with the `w`, `a`, `s`, `d`, `q`, and `e` keys.
The viewing direction can also be changed by dragging
the mouse on the window.

After choosing the desired scene you can focus the
ray-tracing window and press `r` which starts the
ray-tracing. When the output image appears on the
screen you can change the type of the image with `i`.
The application cycles through the following image types:

* Normal rendering
* Normal vectors encoded in rgb
* Barycentric coordinates in rgb
* Depth (distance of the object to the camera)
* Number of triangle checks
* Diff of triangle checks (comparing the last two rendered scenes)
* Number of bounding box checks
* Diff of bounding box checks (comparing the last two rendered scenes)

The lighter the color in the images showing
the number of checks the more checks were performed
to compute this pixel. In the comparison view red
means there were more checks in the current scene
and blue means there were more checks in the previous
scene.

