package com.xulusoft.FaceFinder;

public class HaarRect{
	public int x,y,width,height;
	public double weight=1.0;
	
	public HaarRect() {
		
	}
	
	public HaarRect(int xV,int yV, int w, int h) {
		x=xV;y=yV;width=w;height=h;
	}
	
	public HaarRect(int xV,int yV, int w, int h, double wght) {
		x=xV;y=yV;width=w;height=h;weight=wght;
	}
	
	public HaarRect(String str) {
		String[] arr=str.split(" ");
		x=Integer.parseInt(arr[0]);
		y=Integer.parseInt(arr[1]);
		width=Integer.parseInt(arr[2]);
		height=Integer.parseInt(arr[3]);
		weight=Double.parseDouble(arr[4]);
	}
}