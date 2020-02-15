package imageapprox;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;


public class InputListener implements KeyListener, MouseListener, MouseMotionListener{
   
	ArrayList<Integer> keysPressed = new ArrayList<Integer>();
	KeyEvent keyPressed  = null;//capped at one at a time for simplicity
	boolean isKeyPressed = false;
	
	boolean mouseClicked = false;
	MouseEvent mouseEvent = null;
	
	public InputListener() {
    }

	@Override
	public void keyPressed(KeyEvent e) {
		for(int i : keysPressed) {
			if(e.getKeyCode() == i) return;
		}
		keysPressed.add(e.getKeyCode());

		keyPressed = e;
		isKeyPressed = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for(int idx  = 0; idx < keysPressed.size(); idx++) {
			int j = keysPressed.get(idx);
			if(j == e.getKeyCode()) {
				keysPressed.remove(idx);
				idx--;
			}
		}
	}
	public ArrayList<Integer> getKeysPressed() {
		return keysPressed;
	}
	public boolean isKeyPressed() {
		boolean r = false;
		if(isKeyPressed) {r = true;}
		isKeyPressed = false;
		return r;
	}
	public KeyEvent getPressed() {
		return keyPressed;
	}
	
	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	
	public boolean getMouseClicked() {
		boolean r = false;
		if(mouseClicked) {r = true;}
		mouseClicked = false;
		return r;
	}
	public MouseEvent getMouseEvent() {
		return mouseEvent;
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		mouseClicked = true;
		mouseEvent = e;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	

	
	
}
