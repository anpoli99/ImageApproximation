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

import imageapprox.ImageApprox.Shape;

@SuppressWarnings("unused")
public class ImageApproxSingleIn {
	
	public static final int S_HEIGHT = 1240;
	public static final int S_WIDTH = 1980;
	public static final long TICK_RATE = 1;
	
	static boolean paint_testing = true;
	
	static enum Shape {
		CIRCLE, TRIANGLE, SQUARE, RECT, N_POLY
	}
	static Shape shape = Shape.CIRCLE;
	static int n = 4; //only use if shape = Shape.N_POLY
	
	public static BufferedImage bfi = null;
	public static BufferedImage base = null;
	public static String fileName = "src/images/_.jpg";
	static String loadFile = "src/saves/_.txt";
	static String saveFile = "src/saves/_.txt";
	static String imageSave = "src/saves/_.png";
	
	static int img_x = 256, img_y = 256;
	static int img_max_x = 400, img_max_y = 400;
	static double[] image_data;
	
	static int n_size = 50; //population size
	static int shapes = 200; //number of shapes
	static int points = 3;   //number of points/edges for each shape
	static double p_survive = .02;
	static double[] loss = new double[n_size];
	static double minloss = 10;
	static double max_loss = 0;
	
	static int training_it = 150;
	static int adding_idx = 0;
	
	static int[][][] points_x = new int[n_size][shapes][points];
	static int[][][] points_y = new int[n_size][shapes][points];
	static int[][] red = new int[n_size][shapes];
	static int[][] blue = new int[n_size][shapes];
	static int[][] green = new int[n_size][shapes];
	static int[][] alpha = new int[n_size][shapes];
	static int[][] radii = new int[n_size][shapes];
	
	static int[][] curr_x = new int[n_size][points];
	static int[][] curr_y = new int[n_size][points];
	static int[] curr_r = new int[n_size];
	static int[] curr_g = new int[n_size];
	static int[] curr_b = new int[n_size];
	static int[] curr_a = new int[n_size];
	static int[] curr_rad = new int[n_size];
	
	
	static int var_col = 50;
	static int var_pts = 50;
	static double p_mutation = .05;
	
	static JFrame frame;
	static InputListener k;
	

	public static boolean trn, tst, ptr, tsb, tri;
	public static Button train, save, load, test, save_image;
	public static JLabel howmanytrn, traincount, filenamelabel, loss_label, enterpath, load_complete;
	public static String entpath = "";
	public static int howmany = 0;
	
	public static int random(int i) {
		return (int)(Math.random() * i);
	}
	public static void ran() {
		
		for(int i = 0; i < n_size; i++) {
			for(int j = 0; j < shapes; j++) {
				for(int k = 0; k < points; k++) {
					points_x[i][j][k] = (int)(Math.random() * img_x);
					points_y[i][j][k] = (int)(Math.random() * img_y);
				}
				red[i][j] = (int)(Math.random() * 256);
				blue[i][j] = (int)(Math.random() * 256);
				green[i][j] = (int)(Math.random() * 256);
				
				alpha[i][j] = 255;
				
				radii[i][j] = (int)(Math.random() * Math.max(img_x, img_y) * .25);
			}
		}
	}
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
		
		Image im2 = bfi.getScaledInstance(img_max_x, img_max_y, Image.SCALE_DEFAULT);
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
			/*	image_data[idx] = ((rgb >> 16) & 0xFF) / 256.0; idx++;//red 
				image_data[idx] = ((rgb >>  8) & 0xFF) / 256.0; idx++;//green
				image_data[idx] = ((rgb      ) & 0xFF) / 256.0; idx++;//blue*/
			}
		}
	}
	public static void test(int idx) {
		
		BufferedImage bi = new BufferedImage(img_x, img_y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bi.createGraphics();
		bGr.drawImage(base,0 ,0, null);
		bGr.setColor(Color.BLACK);
		
		bGr.setColor(new Color(curr_r[idx], curr_g[idx], curr_b[idx], curr_a[idx]));
		switch(shape) {
		case TRIANGLE:
		case N_POLY:
			bGr.fillPolygon(curr_x[idx], curr_y[idx], points);
			break;
		case CIRCLE:
			bGr.fillOval(curr_x[idx][0], curr_y[idx][0], curr_rad[idx], curr_rad[idx]);
			break;
		case SQUARE:
			bGr.fillRect(curr_x[idx][0], curr_y[idx][0], curr_rad[idx], curr_rad[idx]);
			break;
		case RECT:
			bGr.fillRect(curr_x[idx][0], curr_y[idx][0], Math.abs(curr_x[idx][1] - curr_x[idx][0]), Math.abs(curr_y[idx][1] - curr_y[idx][0] ));
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
	public static void run() {
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
		int[][] x_clone = new int[n_size][points];
		int[][] y_clone = new int[n_size][points];
		int[] red_clone = new int[n_size];
		int[] blue_clone = new int[n_size];
		int[] green_clone = new int[n_size];
		int[] alpha_clone = new int[n_size];
		int[] radii_clone = new int[n_size];
		int idx = 0;
		for(int i = 0; i < survivors.length; i++) {
			for(int k = 0; k < points; k++) {
				x_clone[idx][k] = curr_x[survivors[i]][k];
				y_clone[idx][k] = curr_y[survivors[i]][k];
			}
			red_clone[idx] = curr_r[survivors[i]];
			blue_clone[idx] = curr_b[survivors[i]];
			green_clone[idx] = curr_g[survivors[i]];
			alpha_clone[idx] = curr_a[survivors[i]];
			radii_clone[idx] = curr_rad[survivors[i]];
			idx++;
		}
		while(idx < n_size) {
			if(Math.random() > .0) {
				//inherit all from 1 parent + random mutations
				int p1 = survivors[(int)(Math.random() * survivors.length)];
			//	for(int j = 0; j < shapes; j++) {
				for(int k = 0; k < points; k++) {
					x_clone[idx][k] = curr_x[p1][k];
					if(Math.random() < p_mutation) {x_clone[idx][k] += (var_pts - 2 * var_pts * Math.random());}
					y_clone[idx][k] = curr_y[p1][k];
					if(Math.random() < p_mutation) {y_clone[idx][k] += (var_pts - 2 * var_pts * Math.random());}

				}
				red_clone[idx] = curr_r[p1];
				if(Math.random() < p_mutation) {red_clone[idx] += (var_col - 2 * var_col * Math.random());}
				blue_clone[idx] = curr_b[p1];
				if(Math.random() < p_mutation) {blue_clone[idx] += (var_col - 2 * var_col * Math.random());}
				green_clone[idx] = curr_g[p1];
				if(Math.random() < p_mutation) {green_clone[idx] += (var_col - 2 * var_col * Math.random());}
				alpha_clone[idx] = curr_a[p1];
				if(Math.random() < p_mutation) {alpha_clone[idx] += (var_col - 2 * var_col * Math.random());}
				radii_clone[idx] = curr_rad[p1];
				if(Math.random() < p_mutation) {radii_clone[idx] += (var_pts - 2 * var_pts * Math.random());}
			
			}
			idx++;
		}
		
		for(int i = 0; i < n_size; i++) {
			for(int k = 0; k < points; k++) {
				curr_x[i][k] = x_clone[i][k];
				if(curr_x[i][k] < 0) {curr_x[i][k] = 0;}
				if(curr_x[i][k] > img_x) {curr_x[i][k] = img_x - 1;}
				curr_y[i][k] = y_clone[i][k];
				if(curr_y[i][k] < 0) {curr_y[i][k] = 0;}
				if(curr_y[i][k] > img_y) {curr_y[i][k] = img_y - 1;}
			}
			curr_r[i] = red_clone[i];
			if(curr_r[i] < 0) {curr_r[i] = 0;}
			if(curr_r[i] > 255) {curr_r[i] = 255;}
			curr_b[i] = blue_clone[i];
			if(curr_b[i] < 0) {curr_b[i] = 0;}
			if(curr_b[i] > 255) {curr_b[i] = 255;}
			curr_g[i] = green_clone[i];
			if(curr_g[i] < 0) {curr_g[i] = 0;}
			if(curr_g[i] > 255) {curr_g[i] = 255;}
			curr_a[i] = alpha_clone[i];
			if(curr_a[i] < 0) {curr_a[i] = 0;}
			if(curr_a[i] > 255) {curr_a[i] = 255;}
			curr_rad[i] = radii_clone[i];
		}
	}
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
				if(Math.random() < p_mutation) {radii_clone[idx][j] += (var_col - 2 * var_col * Math.random());}
			}
			
			idx++;
		}
		
		points_x = points_x_clone;
		points_y = points_y_clone;
		red = red_clone;
		blue = blue_clone;
		green = green_clone;
		alpha = alpha_clone;
		radii = radii_clone;
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
	public static void drawBest(int idx) {
		BufferedImage bi = new BufferedImage(img_x, img_y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bi.createGraphics();
		bGr.drawImage(base,0 ,0, null);
		bGr.setColor(new Color(curr_r[idx], curr_g[idx], curr_b[idx], curr_a[idx]));
		switch(shape) {
		case TRIANGLE:
		case N_POLY:
			bGr.fillPolygon(curr_x[idx], curr_y[idx], points);
			break;
		case CIRCLE:
			bGr.fillOval(curr_x[idx][0], curr_y[idx][0], curr_rad[idx], curr_rad[idx]);
			break;
		case SQUARE:
			bGr.fillRect(curr_x[idx][0], curr_y[idx][0], curr_rad[idx], curr_rad[idx]);
			break;
		case RECT:
			bGr.fillRect(curr_x[idx][0], curr_y[idx][0], Math.abs(curr_x[idx][1] - curr_x[idx][0]), Math.abs(curr_y[idx][1] - curr_y[idx][0] ));
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
	public static void printIndex(String save) throws IOException {
		BufferedReader read = new BufferedReader(new FileReader("src/saves/indicies.txt"));;
		String data = "";
		while(read.ready()) {
			data += read.readLine() +"\n";
			if(data.contains("#")) {break;}
		}
		read.close();
		PrintWriter outwriter = new PrintWriter(new BufferedWriter(new FileWriter("src/saves/indicies.txt") ) );
		outwriter.println(data);
		outwriter.println(save);
		outwriter.close();
	}
	public static void saveImage() throws IOException{
		File outputfile = new File(imageSave);
		ImageIO.write(DrawPanel.best, "png", outputfile);
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
					points_x[i][j][k] = Integer.parseInt(data.substring(0, data.indexOf(',')));
					data = data.substring(data.indexOf(',') + 1);
					if(data.equals("")) {data = read.readLine();}
					points_y[i][j][k] = Integer.parseInt(data.substring(0, data.indexOf(',')));
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

		long yourmilliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
		Date resultdate = new Date(yourmilliseconds);
		load_complete.setText("Load complete (" + sdf.format(resultdate) + ")");
	}
	
	public static void main(String[] args) throws IOException {
		
		init();
		Timer t = new Timer();

		base = new BufferedImage(img_x, img_y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = base.createGraphics();
		bGr.setColor(Color.BLACK);
		int[] corners_x = {0,img_x,img_x,0}, corners_y = {0,0,img_y,img_y};
		bGr.fillPolygon(corners_x, corners_y, 4);
		
		while(adding_idx < shapes) {
			int j = 0;
			for(int i = 0; i < n_size; i++) {
				for(int k = 0; k < points; k++) {
					curr_x[i][k] = points_x[i][adding_idx][k];
					curr_y[i][k] = points_y[i][adding_idx][k];
				}
				curr_r[i] = red[i][adding_idx] ;
				curr_g[i] = green[i][adding_idx] ;
				curr_b[i] = blue[i][adding_idx] ; //set curr
				curr_a[i] = alpha[i][adding_idx] ;
				curr_rad[i] = radii[i][adding_idx];
			}
			double minlossnow = Double.valueOf(minloss); int iter = 0;
			while(j < training_it) {
				
				long time = System.currentTimeMillis();
				traincount.setText(Integer.toString(j) + "," + adding_idx);
				run();
				if(minlossnow > minloss) {
					iter = 0; 
					minlossnow = Double.valueOf(minloss); 
				}
				if(iter > 25) {System.out.println(j + " J"); j = training_it;}
				iter++;
				System.out.println(System.currentTimeMillis() - time);
				j++;
			}
			for(int i = 0; i < n_size; i++) {
				for(int k = 0; k < points; k++) {
					points_x[i][adding_idx][k] = curr_x[0][k];
					points_y[i][adding_idx][k] = curr_y[0][k];
				}
				red[i][adding_idx] = curr_r[0];
				green[i][adding_idx] = curr_g[0]; //set base
				blue[i][adding_idx] = curr_b[0];
				alpha[i][adding_idx] = curr_a[0];
				radii[i][adding_idx] = curr_rad[0];
			}
			adding_idx++;
			bGr.setColor(new Color(curr_r[0],curr_g[0], curr_b[0], curr_a[0]));
			switch(shape) {
			case TRIANGLE:
			case N_POLY:
				bGr.fillPolygon(curr_x[0], curr_y[0], points);
				break;
			case CIRCLE:
				bGr.fillOval(curr_x[0][0], curr_y[0][0], curr_rad[0], curr_rad[0]);
				break;
			case SQUARE:
				bGr.fillRect(curr_x[0][0], curr_y[0][0], curr_rad[0], curr_rad[0]);
				break;
			case RECT:
				bGr.fillRect(curr_x[0][0], curr_y[0][0], Math.abs(curr_x[0][1] - curr_x[0][0]), Math.abs(curr_y[0][1] - curr_y[0][0] ));
			}
		}
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
