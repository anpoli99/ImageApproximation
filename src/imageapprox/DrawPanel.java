package imageapprox;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class DrawPanel extends JPanel implements MouseMotionListener, ActionListener, MouseListener
{ 
  
	
	private static final long serialVersionUID = 1L;
    
  
	public static BufferedImage bufferedImage, bufferedImage2; 
	public static BufferedImage testing, best;
	//String fileName = "images/birds/download (1).jpg";
	
  /** 
   * Draws on a Graphics object 
   * 
   * @param g  a Graphics object 
   */ 
	private void paintMe(Graphics g) throws IOException { 
	 
		
		File file = new File(ImageApprox.fileName);
		
		if (!file.canRead())
		{
			// try adding the media path 
			file = new File(FileChooser.getMediaPath(ImageApprox.fileName));
			if (!file.canRead())
			{
				throw new IOException(ImageApprox.fileName
						+ " could not be opened. Check that you specified the path");
			}
		}	
      
		bufferedImage = ImageIO.read(file);
		if(!(bufferedImage2 == null)) {
			g.drawImage(bufferedImage2, 100, 100 , this);
		}else {

			g.drawImage(bufferedImage, 100, 100 , this);
		}
		if(!(testing == null)) {
			g.drawImage(testing, bufferedImage2.getWidth() + 200, 100, this);
		}
		if(!(best == null)) {
			g.drawImage(best, bufferedImage2.getWidth() * 2 + 300, 100, this);
		}
		
	} 
	
	
  /** 
   * Overrides JPanel's paintComponent method 
   * 
   * @param g  a Graphics object 
   */ 
  public void paintComponent( Graphics g ) 
  { 
    super.paintComponent( g ); 
    try {
		paintMe( g );
	} catch (IOException e) {
		System.err.println("Error reading file!");
		//ImageApprox.fileName = ImageApprox;
		e.printStackTrace();
	} 
  }

    @Override
	public void actionPerformed(ActionEvent arg0) {
	// TODO Auto-generated method stub
    	repaint();
  }

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	} 
}
