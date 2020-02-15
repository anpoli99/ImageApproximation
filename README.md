# ImageApproximation

This project uses a genetic algorithm to approximate a given input image, by mutating a randomly initialized set of shapes. The best from the last generation are randomally mutated, and compared to each image via Mean-squared Error (MSE). The ones with the lowest MSE loss are passed to the next generation. Originally the algorithm was designed with support for crossing-over and cross-mutations, however after experimentation, it was determined that mutating a single member of the population was most effective.
##Usage

There are two classes. ImageApproximation uses a purely genetic algorithm to approximate an image. However, if the algorithm needs to move one shape, it could try hundreds of "bad ideas" before moving that shape. Motivated by this, ImageApproxSingleIn places each image one at a time. The result is that images converge much more quickly, however shapes often do not overlap, leading to less detail and a higher net loss. 

This implenations supports circles, triangles, squares, rectangles and polygons with n-sides (not necessarily simple). 

##Parameters: 
**shape** (**Shape** enum): determines the shapes used for approximation.

**fileName** is the location of the image to be approximated. 
**loadFile/saveFile** are where the parameters of the current training session can be loaded/saved as a .txt file.
**imageSave** is the location where the new image will be saved.

**loadScale**: when an image is loaded, all locations will be scaled by this number (used for upscaling/downscaling an image).
**img_x/img_y**: the image will preserve ratio, but the maximum of its x/y dimensions are determined by these variables.

**n_size**: the number of mutations per generation
**shapes**: the number of shapes used in each approximation.
**p_survive**: the proportion of shapes that will be passed onto the next generation.
