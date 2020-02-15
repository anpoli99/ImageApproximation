# ImageApproximation

This project uses a genetic algorithm to approximate a given input image, by mutating a randomly initialized set of shapes. The best from the last generation are randomally mutated, and compared to each image via Mean-squared Error (MSE). The ones with the lowest MSE loss are passed to the next generation. Originally the algorithm was designed with support for crossing-over and cross-mutations, however after experimentation, it was determined that mutating a single member of the population was most effective.

## Usage

There are two classes. ImageApproximation uses a purely genetic algorithm to approximate an image. However, if the algorithm needs to move one shape, it could try hundreds of "bad ideas" before moving that shape. Motivated by this, ImageApproxSingleIn places each image one at a time. The result is that images converge much more quickly, however shapes often do not overlap, leading to less detail and a higher net loss. 

![Mutates all shapes simultaneously](/saves/glacier1.png)
![Places one shape at a time](/saves/glacier2.png)


This implementation supports circles, triangles, squares, rectangles and polygons with n-sides (not necessarily simple). 

## Parameters: 
*Shape* **shape**: determines the shapes used for approximation.

*String* **fileName** is the location of the image to be approximated. 

*String* **loadFile/saveFile** are where the parameters of the current training session can be loaded/saved as a .txt file.

*String* **imageSave** is the location where the new image will be saved.

*String* **loadScale**: when an image is loaded, all locations will be scaled by this number (used for upscaling/downscaling an image).

*int* **img_x/img_y**: the image will preserve ratio, but the maximum of its x/y dimensions are determined by these variables.

*int* **n_size**: the number of mutations per generation

*int* **shapes**: the number of shapes used in each approximation.

*double* **p_survive**: the proportion of shapes that will be passed onto the next generation.
