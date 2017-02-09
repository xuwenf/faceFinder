package com.xulusoft.FaceFinder;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HaarCascade {
	
	public double Scale = 1/1.2;
	
	public Stage[] stages;

	public int 		SizeX=0,SizeY=0;
	
	public 			HaarCascade() {		
	}

	public 			HaarCascade(String fileName) {
		ReadHaarCascade(fileName);
	}
	
	public 			HaarCascade(InputStream is) {
		ReadHaarCascade(is);
	}
	
	public void 	ReadHaarCascade(String fileStr){
		try {
			InputStream is = new FileInputStream(fileStr);
			ReadHaarCascade(is);
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public void 	ReadHaarCascade(InputStream is){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = factory.newDocumentBuilder();
            try {
				doc = builder.parse(new InputSource(is));
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
           
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();
            // Create XPath object
            XPath xpath = xpathFactory.newXPath();
            String sizeStr = ReadStringElement(doc,xpath, "//size").trim();
            String[] arr=sizeStr.split(" ");
            SizeX=Integer.parseInt(arr[0]);
            SizeY=Integer.parseInt(arr[1]);
            //System.out.println(SizeX+":"+SizeY);
            ReadStages(doc, xpath);
        }
        catch(ParserConfigurationException e) {
        	e.printStackTrace();
        }
        finally {
        	
        }
	}
	
	
	public String 	ReadStringElement(Document doc, XPath xpath, String xpStr ) {
        String result = null;
        try {
            XPathExpression xp = xpath.compile(xpStr);
            result = (String) xp.evaluate(doc, XPathConstants.STRING);
        } catch (Exception ee) {
        }
        return result;
    }
	
	public void 	ReadStages(Document doc, XPath xpath) {
		XPathExpression expr;
		try {
			//expr = xpath.compile(nodeName+"/stages/_");
			expr = xpath.compile("//stages/_");
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			stages = new Stage[nodes.getLength()];
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node=nodes.item(i);
				if (node != null)
					stages[i] = new Stage(node);
				else
					stages[i] = null;
			}
		} catch (XPathExpressionException e) {
			System.out.println("error read stages! ");

			e.printStackTrace();
		}
	}
	
	public class 	Feature{
		public Vector<HaarRect> rects=new Vector<HaarRect>();
		public double tilted=0;
		public Feature(Node fNode) {
			ReadFeature(fNode);
		}
		public void ReadFeature(Node rNode) {
			NodeList pnl=rNode.getChildNodes();
			for (int i=0; i< pnl.getLength();i++) {
				String str =pnl.item(i).getNodeName(); 
				if (str=="rects") {
					NodeList nl = pnl.item(i).getChildNodes();
					int len =nl.getLength();
					for (int j=0;j<len; j++) {
						String str1=nl.item(j).getNodeName().trim();
						if(str1=="_") {
							String rstr=nl.item(j).getTextContent().trim();
							rects.add(new HaarRect(rstr));
						}
					}				
				}
				if (pnl.item(i).getNodeName()=="tilted") tilted = Double.parseDouble(pnl.item(i).getTextContent());
			}
		}
	}
	
	public class 	Tree{
		public Feature feature;;
		public double threshold=0, left_val=0, right_val=0;
		public Tree() {
			
		}
		public Tree(Node fNode) {
			ReadTree(fNode);
		}
		
		public void ReadTree(Node fNode) {			
			NodeList nl = fNode.getChildNodes();
			for (int i=0;i<nl.getLength();i++) {
				String str=nl.item(i).getNodeName();
				if (str=="_") {
					NodeList cnl = nl.item(i).getChildNodes();
					for(int j=0;j<cnl.getLength();j++) {
						String str1 = cnl.item(j).getNodeName();
						if (str1=="feature") feature = new Feature(cnl.item(j));
						if (str1=="threshold") threshold = Double.parseDouble(cnl.item(j).getTextContent().trim());
						if (str1=="left_val") left_val = Double.parseDouble(cnl.item(j).getTextContent().trim());
						if (str1=="right_val") right_val = Double.parseDouble(cnl.item(j).getTextContent().trim());
					}
				}
			}
		}
		
	}
	
	public class 	Stage{
		public Vector<Tree> trees = new Vector<Tree>();
		public double threshold =0;
		public int parent =-1, next=-1;
		public Stage() {
			
		}
		public Stage(Node stageNode) {
			ReadStage(stageNode);
		}
		private void ReadTrees(Node node) {
			NodeList tds = node.getChildNodes();
			for (int i=0; i<tds.getLength();i++) {
				if (tds.item(i).getNodeName()=="_") {
					trees.add(new Tree(tds.item(i)));
				}
			}
		}
		
		public void ReadStage(Node stageNode) {
			NodeList nl = stageNode.getChildNodes();
			for (int i=0;i<nl.getLength();i++) {
				String str=nl.item(i).getNodeName();
				if (str == "trees") ReadTrees(nl.item(i)); 
				if (str=="stage_threshold") threshold = Double.parseDouble(nl.item(i).getTextContent());
				if (str=="parent") parent = Integer.parseInt(nl.item(i).getTextContent());
				if (str=="next") next = Integer.parseInt(nl.item(i).getTextContent());
			}
		}
		
		
	}
}
