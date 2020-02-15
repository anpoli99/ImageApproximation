package imageapprox;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;

@SuppressWarnings("unused")
public class ImageApprox {
	
	static enum Shape {
		CIRCLE, TRIANGLE, SQUARE, RECT, N_POLY
	}
	static Shape shape = Shape.RECT; //shape to reconstruct image
	static int n = 4; //only use if shape = Shape.N_POLY
	
	public static BufferedImage bfi = null;

	public static String fileName = "src/images/_.jpg"; //path to image
	static String loadFile = "src/saves/_.txt";		 	//file to save param of shape
	static String saveFile = "src/saves/_.txt"; 		//file to load param of shape
	static String imageSave = "src/saves/_.png";		//location where image will be saved ### SAVE AS PNG
	
	//control size of frame
	public static final int S_HEIGHT = 1240; 
	public static final int S_WIDTH = 1980;
	public static final long TICK_RATE = 1;
	
	static boolean paint_testing = true; //toggle whether to draw current image during comparisons-runs faster if false
	static boolean remove_hairs = true; //cleans image by removing shapes invisible at lower resolutions
	
	static double load_scale = 1;	//image loaded from txt gets scaled by this
	static int img_x =(int)(256 * load_scale), img_y = (int)( 256 * load_scale), color_chan = 3; //max x/y size for image
	static int img_max_x = 400, img_max_y = 400; //max display size for image
	static double[] image_data;
	
	static int n_size = 50; //population size
	static int shapes = 150; //number of shapes
	static int points = 3;   //number of points/edges for each shape
	static double p_survive = .1; //percent that move to next generaion
	static double[] loss = new double[n_size];
	static double minloss = -1;
	static double max_loss = 0;
	
	//stores information about each generation
	static int[][][] points_x;
	static int[][][] points_y;
	static int[][] radii = new int[n_size][shapes];
	static int[][] red = new int[n_size][shapes];
	static int[][] blue = new int[n_size][shapes];
	static int[][] green = new int[n_size][shapes];
	static int[][] alpha = new int[n_size][shapes];
	
	static int var_col = 100;	//maximum variance in color values
	static int var_pts = 50;	//maximum variance in position
	static double p_mutation = .001;
	
	static JFrame frame;
	static InputListener k;
	
	public static boolean trn, tst, ptr, tsb, tri;
	public static Button train, save, load, test, save_image;
	public static JLabel howmanytrn, traincount, filenamelabel, loss_label, enterpath, load_complete;
	public static String entpath = "";
	public static int howmany = 0;
	
	/*
	 * gets random int in range [0, i)
	 */
	public static int random(int i) {
		return (int)(Math.random() * i);
	}
	
	/*
	 * initializes all generatiosn to random
	 */
	public static void ran() {
		for(int i = 0; i < n_size; i++) {
			for(int j = 0; j < shapes; j++) {
				for(int k = 0; k < points; k++) {
					points_x[i][j][k] = (int)(Math.random() * img_x);
					points_y[i][j][k] = (int)(Math.random() * img_y);
				}
				radii[i][j] = (int)(Math.random() *  Math.max(img_x, img_y) * .25);
				red[i][j] = (int)(Math.random() * 256);
				blue[i][j] = (int)(Math.random() * 256);
				green[i][j] = (int)(Math.random() * 256);
				alpha[i][j] = (int)(Math.random() * 256);
				//alpha[i][j] = 255;
			}
		}
	}
	/*
	 * initializes generations, UI
	 */
	public static void init() throws IOException {
		switch(shape) {
		case TRIANGLE: points = 3; break;
		case N_POLY: points = n; break;
		case CIRCLE:
		case SQUARE: points = 1; break;
		case RECT: points = 2; break;
		}
		points_x = new int[n_size][shapes][points];
		points_y = new int[n_size][shapes][points];;
		input();
		ran();
		frame = new JFrame( "ImageIdentificationLab" ); 
		
		train = new Button(" TRAIN", 100,600,200,50);
		trn = false;
		frame.add(train.getLabel());
			
		howmanytrn = new JLabel();
		howmanytrn.setFont(new Font("Verdana",1,25));
		howmanytrn.setBounds(100,800,400,100);
		frame.add(howmanytrn);
		
		traincount = new JLabel();
		traincount.setFont(new Font("Verdana",1,25));
		traincount.setBounds(100,900,400,100);
		frame.add(traincount);
		
		filenamelabel = new JLabel();
		filenamelabel.setFont(new Font("Verdana",1,25));
		filenamelabel.setBounds(600,300,1000,100);
		frame.add(filenamelabel);
		
		loss_label = new JLabel();
		loss_label.setFont(new Font("Verdana",Font.BOLD,25));
		loss_label.setBounds(1600,200,1000,100);
		frame.add(loss_label);
	
		load_complete = new JLabel();
		load_complete.setFont(new Font("Verdana",Font.BOLD,25));
		load_complete.setBounds(1000,1000,1000,100);
		frame.add(load_complete);
		
		test = new Button(" TEST", 500,600,200,50);
		tst = false;
		frame.add(test.getLabel());
		
		save_image = new Button(" SAVE_IMAGE", 1000,600,200,50);
		frame.add(save_image.getLabel());
		
		save = new Button(" SAVE", 1200,600,200,50);
		frame.add(save.getLabel());
		
		load = new Button(" LOAD", 1400,600,200,50);
		frame.add(load.getLabel());
		
			
		enterpath = new JLabel();
		enterpath.setFont(new Font("Verdana",1,25));
		enterpath.setBounds(500,700,1000,100);
		frame.add(enterpath);
			
		frame.getContentPane().add( new DrawPanel() ); 
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 
		frame.pack(); 
		frame.setSize( S_WIDTH, S_HEIGHT ); 
		frame.setLocation( 100, 100 ); 
		frame.setVisible( true ); 
	
		k = new InputListener();
		frame.addKeyListener(k);
		frame.addMouseListener(k);
	}
	/*
	 * gets input of image
	 */
	public static void input() throws IOException {
		File file = new File(fileName);
		
		if (!file.canRead())
		{
			// try adding the media path 
			file = new File(FileChooser.getMediaPath(fileName));
			if (!file.canRead())
			{
				throw new IOException(fileName
						+ " could not be opened. Check that you specified the path");
			}
		}	

		bfi = ImageIO.read(file);
		BufferedImage bfi2 = ImageIO.read(file);
	
		Image im;

		if(bfi.getHeight() <= bfi.getWidth()) {
			im = bfi.getScaledInstance(img_x, img_x * bfi.getHeight() / bfi.getWidth(), Image.SCALE_DEFAULT);
		}else {
			im = bfi.getScaledInstance(img_y * bfi.getWidth()/ bfi.getHeight(), img_y, Image.SCALE_DEFAULT);
		}
		bfi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D bGr = bfi.createGraphics();
		bGr.drawImage(im, 0, 0, null);
		bGr.dispose();
		
		img_x = bfi.getWidth();
		img_y = bfi.getHeight();
		if(img_x > img_y) {
			img_max_y = img_y * img_max_x / img_x;
		}else {
			img_max_x = img_x * img_max_y / img_y;
		}
		
		Image im2 = bfi.getScaledInstance(bfi2.getWidth(), bfi2.getHeight(), Image.SCALE_DEFAULT);
		if(im2.getWidth(null) > img_max_x || im2.getHeight(null) > img_max_y) {
			im2 = bfi.getScaledInstance(img_max_x, img_max_y, Image.SCALE_DEFAULT);
		}
		BufferedImage b2 = new BufferedImage(im2.getWidth(null), im2.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		
		
		Graphics2D bGr2 = b2.createGraphics();
		bGr2.drawImage(im2, 0, 0, null);
		bGr2.dispose();
		DrawPanel.bufferedImage2 = b2;
		
		int idx = 0;
		for(int i = 0; i < img_x && i < bfi.getWidth(); i++) {
			for(int j = 0; j < img_y && j < bfi.getHeight(); j++) {
				int rgb = bfi.getRGB(i, j);
				if(((rgb >> 16) & 0xFF) > 127) {//red
					max_loss += Math.pow(((rgb >> 16) & 0xFF) , 2); 
				}else {
					max_loss += Math.pow(255 - ((rgb >> 16) & 0xFF) , 2);
				}
				if(((rgb >>  8) & 0xFF) > 127) {//green
					max_loss += Math.pow(((rgb >>  8) & 0xFF) , 2);
				}else {
					max_loss += Math.pow(255 - ((rgb >>  8) & 0xFF) , 2);
				}
				if(((rgb      ) & 0xFF) > 127) {//blue
					max_loss += Math.pow(((rgb      ) & 0xFF) , 2);
				}else {
					max_loss += Math.pow(255 - ((rgb      ) & 0xFF) , 2);
				}
			}
		}
	}
	/*
	 * tests a generation
	 */
	public static void test(int idx) {
		
		BufferedImage bi = new BufferedImage(img_x, img_y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bi.createGraphics();
		bGr.setColor(Color.BLACK);
		int[] corners_x = {0,img_x,img_x,0}, corners_y = {0,0,img_y,img_y};
		bGr.fillPolygon(corners_x, corners_y , 4);
		for(int i = 0; i < shapes; i++) {
			bGr.setColor(new Color(red[idx][i], green[idx][i], blue[idx][i], alpha[idx][i]));
			switch(shape) {
			case TRIANGLE:
			case N_POLY:
				bGr.fillPolygon(points_x[idx][i], points_y[idx][i], points);
				break;
			case CIRCLE:
				bGr.fillOval(points_x[idx][i][0], points_y[idx][i][0], radii[idx][i], radii[idx][i]);
				break;
			case SQUARE:
				bGr.fillRect(points_x[idx][i][0], points_y[idx][i][0], radii[idx][i], radii[idx][i]);
				break;
			case RECT:
				bGr.fillRect(points_x[idx][i][0], points_y[idx][i][0], Math.abs(points_x[idx][i][1] - points_x[idx][i][0]), Math.abs(points_y[idx][i][1] - points_y[idx][i][0] ));
				break;
			}
		}
		for(int i = 0; i < img_x; i++) {
			for(int j = 0; j < img_y; j++) {
				int rgb = bfi.getRGB(i, j);
				int rgb2 = bi.getRGB(i, j);
				loss[idx] += Math.pow(((rgb >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF),2); //red 
				loss[idx] += Math.pow(((rgb >>  8) & 0xFF) - ((rgb2 >>  8) & 0xFF),2); //green
				loss[idx] += Math.pow(((rgb      ) & 0xFF) - ((rgb2      ) & 0xFF),2); //blue
			}
		}
		loss[idx] = loss[idx] / max_loss;
		if(paint_testing) {
			Image im2 = bi.getScaledInstance(DrawPanel.bufferedImage2.getWidth(), DrawPanel.bufferedImage2.getHeight(), Image.SCALE_DEFAULT);
			BufferedImage bi2 = new BufferedImage(im2.getWidth(null), im2.getHeight(null), BufferedImage.TYPE_INT_ARGB);

			Graphics2D bGr2 = bi2.createGraphics();
			bGr2.drawImage(im2, 0, 0, null);
			bGr2.dispose();
			DrawPanel.testing = bi2;
			frame.repaint();
		}
	
	}
	/*
	 * adjusts genetations via mutations
	 */
	public static void adj() {
		loss = new double[n_size];
		for(int i = 0; i < n_size; i++) {
			test(i);
		}
		minloss = 0;
		int[] survivors = new int[(int)(p_survive * n_size)];
		for(int j = 0; j < (int)(p_survive * n_size); j++) {
			int idx = 0;
			for(int k = 1; k < loss.length; k++) {
				if(loss[k] < loss[idx] && loss[k] != minloss) {
					idx = k; 
				}  
			}
			if(j == 0) {minloss = loss[idx]; drawBest(idx);}
			loss[idx] = Integer.MAX_VALUE;
			survivors[j] = idx;
		}
		int[][][] points_x_clone = new int[n_size][shapes][points];
		int[][][]  points_y_clone = new int[n_size][shapes][points];
		int[][] red_clone = new int[n_size][shapes];
		int[][] blue_clone = new int[n_size][shapes];
		int[][] green_clone = new int[n_size][shapes];
		int[][] alpha_clone = new int[n_size][shapes];
		int[][] radii_clone = new int[n_size][shapes];
		int idx = 0;
		for(int i = 0; i < survivors.length; i++) {
			for(int j = 0; j < shapes; j++) {
				for(int k = 0; k < points; k++) {
					points_x_clone[idx][j][k] = points_x[survivors[i]][j][k];
					points_y_clone[idx][j][k] = points_y[survivors[i]][j][k];
				}
				red_clone[idx][j] = red[survivors[i]][j];
				blue_clone[idx][j] = blue[survivors[i]][j];
				green_clone[idx][j] = green[survivors[i]][j];
				alpha_clone[idx][j] = alpha[survivors[i]][j];
				radii_clone[idx][j] = radii[survivors[i]][j];
			}
			idx++;
		}
		while(idx < n_size) {
			
			//inherit all from 1 parent + random mutations
			int p1 = survivors[(int)(Math.random() * survivors.length)];
			for(int j = 0; j < shapes; j++) {
				for(int k = 0; k < points; k++) {
					points_x_clone[idx][j][k] = points_x[p1][j][k];
					if(Math.random() < p_mutation) {points_x_clone[idx][j][k] += (var_pts - 2 * var_pts * Math.random());}
					points_y_clone[idx][j][k] = points_y[p1][j][k];
					if(Math.random() < p_mutation) {points_y_clone[idx][j][k] += (var_pts - 2 * var_pts * Math.random());}

				}
				red_clone[idx][j] = red[p1][j];
				if(Math.random() < p_mutation) {red_clone[idx][j] += (var_col - 2 * var_col * Math.random());}
				blue_clone[idx][j] = blue[p1][j];
				if(Math.random() < p_mutation) {blue_clone[idx][j] += (var_col - 2 * var_col * Math.random());}
				green_clone[idx][j] = green[p1][j];
				if(Math.random() < p_mutation) {green_clone[idx][j] += (var_col - 2 * var_col * Math.random());}
				alpha_clone[idx][j] = alpha[p1][j];
				if(Math.random() < p_mutation) {alpha_clone[idx][j] += (var_col - 2 * var_col * Math.random());}
				radii_clone[idx][j] = radii[p1][j];
				if(Math.random() < p_mutation) {radii_clone[idx][j] += (var_pts - 2 * var_pts * Math.random());}
			}
			
			
			idx++;
		}
		
		points_x = points_x_clone;
		points_y = points_y_clone;
		red = red_clone;
		blue = blue_clone;
		green = green_clone;
		alpha = alpha_clone;
		for(int i = 0; i < n_size; i++) {
			for(int j = 0; j < shapes; j++) {
				for(int k = 0; k < points; k++) {
					if(points_x[i][j][k] < 0) {points_x[i][j][k] = 0;}
					if(points_x[i][j][k] > img_x) {points_x[i][j][k] = img_x - 1;}
					if(points_y[i][j][k] < 0) {points_y[i][j][k] = 0;}
					if(points_y[i][j][k] > img_y) {points_y[i][j][k] = img_y - 1;}
					
				}
				if(red[i][j] < 0) {red[i][j] = 0;}
				if(red[i][j] > 255) {red[i][j] = 255;}
				if(blue[i][j] < 0) {blue[i][j] = 0;}
				if(blue[i][j] > 255) {blue[i][j] = 255;}
				if(green[i][j] < 0) {green[i][j] = 0;}
				if(green[i][j] > 255) {green[i][j] = 255;}
				if(alpha[i][j] < 0) {alpha[i][j] = 0;}
				if(alpha[i][j] > 255) {alpha[i][j] = 255;}
			}
		}
	}
	
	/*
	 * draws the best of curr. generation to the screen
	 */
	public static void drawBest(int idx) {
		BufferedImage bi = new BufferedImage(img_x, img_y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bi.createGraphics();
		int[] corners_x = {0,img_x,img_x,0}, corners_y = {0,0,img_y,img_y};
		bGr.setColor(Color.BLACK);
		bGr.fillPolygon(corners_x, corners_y , 4);
		for(int i = 0; i < shapes; i++) {
			bGr.setColor(new Color(red[idx][i], green[idx][i], blue[idx][i], alpha[idx][i]));
			switch(shape) {
			case TRIANGLE:
			case N_POLY:
				bGr.fillPolygon(points_x[idx][i], points_y[idx][i], points);
				break;
			case CIRCLE:
				bGr.fillOval(points_x[idx][i][0], points_y[idx][i][0], radii[idx][i], radii[idx][i]);
				break;
			case SQUARE:
				bGr.fillRect(points_x[idx][i][0], points_y[idx][i][0], radii[idx][i], radii[idx][i]);
				break;
			case RECT:
				bGr.fillRect(points_x[idx][i][0], points_y[idx][i][0], Math.abs(points_x[idx][i][1] - points_x[idx][i][0]), Math.abs(points_y[idx][i][1] - points_y[idx][i][0] ));
				break;
			}
		}
		
		Image im2 = bi.getScaledInstance(DrawPanel.bufferedImage2.getWidth(), DrawPanel.bufferedImage2.getHeight(), Image.SCALE_DEFAULT);
		BufferedImage bi2 = new BufferedImage(im2.getWidth(null), im2.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr2 = bi2.createGraphics();
		bGr2.drawImage(im2, 0, 0, null);
		bGr2.dispose();
		DrawPanel.best = bi2;
		
		loss_label.setText("Min Loss = " + minloss);
		frame.repaint();
	}
	public static void save() throws IOException {
		PrintWriter outwriter = new PrintWriter(new BufferedWriter(new FileWriter(saveFile) ) );
		
		outwriter.println("MinLoss:" + minloss);
		outwriter.println(",n_size:" + n_size + ",shapes:" + shapes + ",points:" + points);
		outwriter.println("img_x:" + img_x + ",img_y:" + img_y);
		outwriter.println("#");
		for(int i = 0; i < n_size; i++) {
			for(int j = 0; j < shapes; j++) {
				for(int k = 0; k < points; k++) {
					outwriter.print(points_x[i][j][k] + "," + points_y[i][j][k] + ",");
				}
				outwriter.println(red[i][j]+ "," + green[i][j]+ "," + blue[i][j]+ "," + alpha[i][j] + ",");
				outwriter.println(radii[i][j]);
			}
		}
		outwriter.close();
		System.out.println("save complete");

		long yourmilliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
		Date resultdate = new Date(yourmilliseconds);
		load_complete.setText("Save complete (" + sdf.format(resultdate) + ")");
		printIndex(saveFile  + "," + sdf.format(resultdate) + ",MinLoss:" + minloss
				+ ",n_size:" + n_size + ",shapes:" + shapes + ",points:" + points +
				",img_x:" + img_x + ",img_y:" + img_y);
	}
	/*
	 * keeps track of saves
	 */
	public static void printIndex(String save) throws IOException {
		BufferedReader read = new BufferedReader(new FileReader("saves/indicies.txt"));;
		String data = "";
		while(read.ready()) {
			data += read.readLine() +"\n";
			if(data.contains("#")) {break;}
		}
		read.close();
		PrintWriter outwriter = new PrintWriter(new BufferedWriter(new FileWriter("saves/indicies.txt") ) );
		outwriter.println(data);
		outwriter.println(save);
		outwriter.close();
	}
	public static void removeHairs() {
		test(0);
		System.out.println("removing hairs...");
		double loss_zero = loss[0];
		
		for(int i = 0; i < shapes; i++) {
			int temp_alpha = alpha[0][i];
			alpha[0][i] = 0;
			test(0);
			if(loss_zero < loss[0]) {
				alpha[0][i] = temp_alpha;
			}else {
				loss_zero = loss[0];
			}
			load_complete.setText(Integer.toString(i));
		}
		System.out.println("complete");
		
	}
	public static void saveImage() throws IOException{
		BufferedImage bi = new BufferedImage(img_x, img_y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bi.createGraphics();
		int[] corners_x = {0,img_x,img_x,0}, corners_y = {0,0,img_y,img_y};
		bGr.setColor(Color.BLACK);
		bGr.fillPolygon(corners_x, corners_y , 4);
		for(int i = 0; i < shapes; i++) {
			bGr.setColor(new Color(red[0][i], green[0][i], blue[0][i], alpha[0][i]));
			bGr.fillPolygon(points_x[0][i], points_y[0][i], points);
		}
		
		
		File outputfile = new File(imageSave);
		ImageIO.write(bi, "png", outputfile);
		long yourmilliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
		Date resultdate = new Date(yourmilliseconds);
		load_complete.setText("Save complete (" + sdf.format(resultdate) + ")");
		
	}
	public static void load() throws IOException {
		BufferedReader read = new BufferedReader(new FileReader(loadFile));
		String data = "";
		System.out.println("beginning load");
		while(read.ready()) {
			data += read.readLine();
			if(data.contains("#")) {break;}
		}
		data = read.readLine();
		if(data.equals("")) {data = read.readLine();}
		for(int i = 0; i < n_size; i++) {
			for(int j = 0; j < shapes; j++) {
				for(int k = 0; k < points; k++) {
					points_x[i][j][k] = (int)(Integer.parseInt(data.substring(0, data.indexOf(','))) * load_scale);
					data = data.substring(data.indexOf(',') + 1);
					if(data.equals("")) {data = read.readLine();}
					points_y[i][j][k] = (int)(Integer.parseInt(data.substring(0, data.indexOf(','))) * load_scale);
					data = data.substring(data.indexOf(',') + 1);
					if(data.equals("")) {data = read.readLine();}
				}
				red[i][j] = Integer.parseInt(data.substring(0, data.indexOf(',')));
				data = data.substring(data.indexOf(',') + 1);
				if(data.equals("")) {data = read.readLine();}
				green[i][j] = Integer.parseInt(data.substring(0, data.indexOf(',')));
				data = data.substring(data.indexOf(',') + 1);
				if(data.equals("")) {data = read.readLine();}
				blue[i][j] = Integer.parseInt(data.substring(0, data.indexOf(',')));
				data = data.substring(data.indexOf(',') + 1);
				if(data.equals("")) {data = read.readLine();}
				alpha[i][j] = Integer.parseInt(data.substring(0, data.indexOf(',')));
				data = data.substring(data.indexOf(',') + 1);
				if(data.equals("")) {data = read.readLine();}
				radii[i][j] = Integer.parseInt(data.substring(0, data.indexOf(',')));
				data = data.substring(data.indexOf(',') + 1);
				if(data.equals("")) {data = read.readLine();}
			}
		}
		
		read.close();
		System.out.println("load complete");
		if(remove_hairs) {removeHairs();}
		long yourmilliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
		Date resultdate = new Date(yourmilliseconds);
		load_complete.setText("Load complete (" + sdf.format(resultdate) + ")");
	}
	
	public static void main(String[] args) throws IOException {
		
		init();
		Timer t = new Timer();
		
		
		TimerTask update = new TimerTask() {
		
			@Override
			public void run() {
				String init = fileName;
				if(trn) {
					trainprep();
				}else if(tst) {
					testfile();
				}else if(k.getMouseClicked()) {
					if(train.inBounds(k.getMouseEvent().getX(), k.getMouseEvent().getY())) {
						trn = true;
					//	train2();
					}else if(test.inBounds(k.getMouseEvent().getX(), k.getMouseEvent().getY())) {
						tst = true;
					}else if(save.inBounds(k.getMouseEvent().getX(), k.getMouseEvent().getY())) {
						try {
							save();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else if(load.inBounds(k.getMouseEvent().getX(), k.getMouseEvent().getY())) {
						try {
							load();

							//removeHairs();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else if(save_image.inBounds(k.getMouseEvent().getX(), k.getMouseEvent().getY())) {
						try {
							saveImage();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					//System.out.println(k.getMouseEvent().getX() + "," + k.getMouseEvent().getY());
				}else if(k.isKeyPressed()) {
					int i = k.getPressed().getKeyCode();
					if(i == KeyEvent.VK_P) {
						ptr = !ptr;
					}
					if(i == KeyEvent.VK_S) {
						tsb = true;
					}
					if(i == KeyEvent.VK_B) {
						tsb = false;
					}
				}	
				
				frame.repaint();
				if(!fileName.equals(init)) {
				//	test();
				}
			}
			public void trainprep() {
				howmanytrn.setVisible(true);
				frame.repaint();
				if(!k.isKeyPressed()) {}// do nothing
				else if(k.getPressed().getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						train();
					} catch (IOException e) {
						e.printStackTrace();
					}
					howmanytrn.setVisible(false);
					trn = false;
				}else {
					if(k.getPressed().getKeyChar() - '0' >= 0 &&
							k.getPressed().getKeyChar() - '0' <= '9' - '0') {
						howmany = (10 * howmany) +  (int)k.getPressed().getKeyChar() - '0';
					}else if(k.getPressed().getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						howmany /= 10;
					}
				}
				howmanytrn.setText("<html>How many?<br/>" + howmany + "</html>");
			}
			public void train() throws IOException {
				traincount.setVisible(true);
				double i1 = 0, i2 = 0;
				while(howmany > 0) {
					long time = System.currentTimeMillis();
					howmany--; 
					traincount.setText(Integer.toString(howmany));
					adj();
					frame.repaint();
					System.out.println(System.currentTimeMillis() - time);
				}
				traincount.setVisible(false);
			}
			public void train2() {
				traincount.setVisible(true);
				double i1 = 0, i2 = 0;
				howmany = 0;
				while(!k.isKeyPressed()) {
					long time = System.currentTimeMillis();
					howmany++;
					traincount.setText(Integer.toString(howmany));
					adj();
					frame.repaint();
					System.out.println(System.currentTimeMillis() - time);
				}
				traincount.setVisible(false);
			}
			public void testfile() {
				if(!k.isKeyPressed()) {}// do nothing
				else if(k.getPressed().getKeyCode() == KeyEvent.VK_ENTER) {
					fileName = entpath;
					tst = false;
					entpath = "";
					enterpath.setText("");
					return;
				}else {
					if(k.getPressed().getKeyCode() == KeyEvent.VK_BACK_SPACE && entpath.length() > 0) {
						entpath = entpath.substring(0, entpath.length() - 1);
					}else if(k.getPressed().getKeyCode() != KeyEvent.VK_SHIFT){
						entpath += k.getPressed().getKeyChar();
					}
				}
				enterpath.setText("<html> Enter path: <br/> " + entpath + "</html>");
			
			}
			
		};
		t.schedule(update, 20, TICK_RATE);
		
		
	}
	
}
