package xmj.minibase01;

import java.util.ArrayList;
import java.util.List;


public class Node {
	public final static int BLOCK_SIZE=4096;
	public static String global_lexer=null;
	public static String global_parser=null;
	public static Node global_syn_tree=null;
	public static Node global_logical_tree=null;
	public String value;
	public List<Object> var;
	public List<Node> children;
	
	public Node(){
		
	}
	public Node(String value,List<Node> children,List<Object> varlist){
		this.value=value;
		this.var=varlist;
		if(children!=null){
			this.children=children;
		}
		else{
			this.children=new ArrayList<>();
		}
	}
	
	public static void show(Node nodeObj){
		if(nodeObj instanceof Node){
			//System.out.println("�������value");
			System.out.println(nodeObj.value);
			//System.out.println("������value");
			if(nodeObj.var!=null){
				//System.out.println("������var");
				System.out.println(nodeObj.var);
				//System.out.println("������var");
			}
			if(nodeObj.children!=null){
				for(int i=0;i<nodeObj.children.size();i++){
					//System.out.println("������children");
					show(nodeObj.children.get(i));
					//System.out.println("������children");
				}
			}
		}
	}
	
	public static void traver(Node nodeobj,List<String> sqlsent){
		sqlsent.add(nodeobj.value);
		if(nodeobj.children.size()!=0){
			for(int i=0;i<nodeobj.children.size();i++){
				traver(nodeobj.children.get(i),sqlsent);
			}
		}
		/*if(nodeobj instanceof Node){
			sqlsent.add(nodeobj.value);
			if(nodeobj.children.size()!=0){
				for(int i=0;i<nodeobj.children.size();i++){
					traver(nodeobj.children.get(i),sqlsent);
				}
			}
		}*/
	}
	public static void get_child(Node nodeobj,List<Object> templist){
		if(nodeobj.children.size()==0){
			System.out.println("���ڽ���ĺ��ӽڵ�");
			Object o1=nodeobj.value;
			templist.add(o1);
		}
		else {
			for(int i=0;i<nodeobj.children.size();i++){
				get_child(nodeobj.children.get(i),templist);
			}
		}
	}
}
