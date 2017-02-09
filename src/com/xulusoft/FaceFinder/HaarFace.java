package com.xulusoft.FaceFinder;

import java.util.Vector;

public class HaarFace {
	public HaarRect FaceRect=null;
	public int X=0, Y=0;
	public HaarRect Eye_Left=null;
	public HaarRect Eye_Right=null;
	public HaarRect Nose=null;
	public HaarRect Mouth=null;
	
	public boolean IsFace() {
		if (Eye_Left != null) return true;
		if (Eye_Right != null) return true;
		if (Mouth != null) return true;
		if (Nose != null) return true;
		return false;
	}
	
	public void SetEyes(Vector<HaarRect> rects) {
		for (int i=0;i<rects.size();i++) {
			int x=0,y=0;
			HaarRect rc = rects.get(i);
			x=rc.x+rc.width/2;
			y=rc.y+rc.height/2;
			if (x<X && y<Y && Eye_Right == null) 	Eye_Right = rc;
			if (x>X && y<Y && Eye_Left == null) 	Eye_Left = rc;
		}
	}
	
	public void SetMouth(Vector<HaarRect> rects) {
		for (int i=0;i<rects.size();i++) {
			int x=0,y=0;
			HaarRect rc = rects.get(i);
			x=rc.x+rc.width/2;
			y=rc.y+rc.height/2;
			if (y>Y+FaceRect.height/5 && Mouth == null) {
				Mouth = rc;
				break;
			}
		}
	}
	
	public void SetNose(Vector<HaarRect> rects) {
		for (int i=0;i<rects.size();i++) {
			int x=0,y=0;
			HaarRect rc = rects.get(i);
			x=rc.x+rc.width/2;
			y=rc.y+rc.height/2;
			if ( y>Y && Nose == null) {
				Nose = rc;
				break;
			}
		}
	}
	
	public HaarFace(HaarRect rect) {
		FaceRect = rect;
		X = rect.x + rect.width/2;
		Y = rect.y + rect.height/2;
	}
}
