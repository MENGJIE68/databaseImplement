package xmj.minibase01;

import java.util.ArrayList;
import java.util.List;

//�����Լ��������sql���ƥ�䵽�˶�Ӧ��ģʽ���������˶�Ӧ��sellist,fromlist.wherelist.orderlist
//����Ҫ������ǽ�����Ӧ������������ȥ�������﷨����ģ��parse_db.py��ֱ���ڸ�����֧����ö�Ӧ�ĺ�������
import java.util.regex.Pattern;

//���ȣ�����������ʽȥƥ�����ģʽ�����ǵ������ÿո�ֿ�Ȼ��ʹ�ó����ж�ģʽ
//where����������ǰ�治һ����һ��������..=..������.. like ..
//����ƥ��
public class Judge {
	public List<Object> sellist;
	public List<Object> fromlist;
	public List<Object> wherelist;
	public List<Object> orderlist;
	
	public List<Object> sel_list(String[] sela1){
		if(sela1.length==1){
			//System.out.println("����Ϊ1");
			List<Object> list1=new ArrayList<>();
			list1.add(sela1[0]);
			return list1;
		}
		else {
			//System.out.println("���ȴ���1");
			List<Object> sellist=new ArrayList<>();
			sellist.add(sela1[0]);
			sellist.add(",");
			String[] sela2=new String[sela1.length-1];
			for(int i=0;i<sela2.length;i++){
				sela2[i]=sela1[i+1];
			}
			List<Object> list2=sel_list(sela2); //�ݹ����ɺ����ֶε��б�
			sellist.add(list2); //��������ѡ���ֶ���Ϊ������Ԫ�أ��Ǹ��б�
			//System.out.println(sellist);
			return sellist;
		}
	}
	public List<Object> from_list(String[] froma1){
		if(froma1.length==1){
			//System.out.println("����Ϊ1");
			List<Object> list1=new ArrayList<>();
			list1.add(froma1[0]);
			return list1;
		}
		else {
			//System.out.println("���ȴ���1");
			List<Object> fromlist=new ArrayList<>();
			fromlist.add(froma1[0]);
			fromlist.add(",");
			String[] froma2=new String[froma1.length-1];
			for(int i=0;i<froma2.length;i++){
				froma2[i]=froma1[i+1];
			}
			List<Object> list2=from_list(froma2); //�ݹ����ɺ����������б�
			fromlist.add(list2);
			//System.out.println(fromlist);
			return fromlist;
		}
	}
	public List<Object> where_list(String[] wherea1){ //wherea1�����ÿ��Ԫ����һ������
		List<Object> wherelist=new ArrayList<>();
		for(int i=0;i<wherea1.length;i++){ //��������ÿһ������
			String wherestr=wherea1[i];
			List<String> list1=new ArrayList<>();
			if(wherestr.matches(".*=.*")){
				//�ǵ�ֵ����С�ڴ�������
				int pos1=wherestr.indexOf("=");
				String attr=wherestr.substring(0, pos1); //��ȡ��ֵ������Ӧ���ֶβ���
				String ch=wherestr.substring(pos1+1); //��ȡ��ֵ������Ӧ�ĳ�������
				list1.add(attr);
				list1.add("=");
				list1.add(ch);
			}
			else if(wherestr.matches(".*like.*")){
				int pos1=wherestr.indexOf("like");
				String attr=wherestr.substring(0, pos1); //��ȡ��ֵ������Ӧ���ֶβ���
				String ch=wherestr.substring(pos1+5); //��ȡ��ֵ������Ӧ�ĳ�������
				list1.add(attr);
				list1.add("like");
				list1.add(ch);
			}
			else if(wherestr.matches(".*>.*")){
				int pos1=wherestr.indexOf(">");
				String attr=wherestr.substring(0, pos1); //��ȡ��ֵ������Ӧ���ֶβ���
				String ch=wherestr.substring(pos1+1); //��ȡ��ֵ������Ӧ�ĳ�������
				list1.add(attr);
				list1.add(">");
				list1.add(ch);
			}
			else if(wherestr.matches(".*<.*")){
				int pos1=wherestr.indexOf("<");
				String attr=wherestr.substring(0, pos1); //��ȡ��ֵ������Ӧ���ֶβ���
				String ch=wherestr.substring(pos1+1); //��ȡ��ֵ������Ӧ�ĳ�������
				list1.add(attr);
				list1.add("<");
				list1.add(ch);
			}
			wherelist.add(list1);
			if(i!=wherea1.length-1){
				wherelist.add(",");
			}
		}
		return wherelist;
	}
	public List<Object> order_list(String[] ordera1){ //ÿ��Ԫ��Ϊ  '����1'  ����   '����1 desc/asc'
		if(ordera1.length==1){
			//System.out.println("����Ϊ1");
			List<Object> list1=new ArrayList<>();
			if((ordera1[0].matches(".*\\sdesc"))||(ordera1[0].matches(".*\\sasc"))){
				//��desc����ascԼ��
				String[] arr=ordera1[0].split(" ");
				List<String> subli=new ArrayList<>();
				subli.add(arr[0]);
				subli.add(arr[1]);
				list1.add(subli);
			}
			else{ //û��desc����ascԼ��
				list1.add(ordera1[0]);
			}
			return list1;
		}
		else {
			//System.out.println("���ȴ���1");
			List<Object> orderlist=new ArrayList<>();
			if((ordera1[0].matches(".*\\sdesc"))||(ordera1[0].matches(".*\\sasc"))){
				//��desc����ascԼ��
				String[] arr=ordera1[0].split(" ");
				List<String> subli=new ArrayList<>();
				subli.add(arr[0]);
				subli.add(arr[1]);
				orderlist.add(subli);
			}
			else{ //û��desc����ascԼ��
				orderlist.add(ordera1[0]);
			}
			
			orderlist.add(",");
			String[] ordera2=new String[ordera1.length-1];
			for(int i=0;i<ordera2.length;i++){
				ordera2[i]=ordera1[i+1];
			}
			List<Object> list2=order_list(ordera2);
			orderlist.add(list2); //��������ѡ���ֶ���Ϊ������Ԫ�أ��Ǹ��б�
			//System.out.println(orderlist);
			return orderlist;
		}
	}
	public Node judge(String str){	
		System.out.println("�������Ϊ"+str);
		List<String> patlist=new ArrayList<>(); //ģʽ�б�
		String pattern1="select\\sdistinct.*from.*where.*order\\sby.*";
		patlist.add(pattern1);
		String pattern2="select.*from.*where.*order\\sby.*";
		patlist.add(pattern2);
		String pattern3="select\\sdistinct.*from.*order\\sby.*";
		patlist.add(pattern3);
		String pattern4="select\\sdistinct.*from.*where.*";
		patlist.add(pattern4);
		String pattern5="select.*from.*order\\sby.*";
		patlist.add(pattern5);
		String pattern6="select.*from.*where.*";
		patlist.add(pattern6);
		String pattern7="select\\sdistinct.*from.*";
		patlist.add(pattern7);
		String pattern8="select.*from.*";
		patlist.add(pattern8);
		int pos=-1;
		for(int i=0;i<patlist.size();i++){
			boolean isMatch = Pattern.matches(patlist.get(i), str);
			if(isMatch){
				pos=i;
				System.out.println("����ƥ���ģʽΪ"+patlist.get(i));
				break;
			}
		}
		if(pos!=-1){ //���sql�����ƥ���ģʽ
			Parse_db par1=new Parse_db();
			if(pos==0){ //��Ӧ��SDFWOģʽ
				//System.out.println("������SDFWOģʽ");
				int p1=str.indexOf("distinct");
				p1+=9; //�ҵ�ѡ������Կ�ʼ��λ��
				int p2=str.indexOf("from"); //�ҵ�from��λ��
				String selarr=str.substring(p1,p2-1); //ѡ�������ַ�������ѡ�����ֶβ���
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				
				p2+=5;
				int p3=str.indexOf("where");
				String fromarr=str.substring(p2,p3-1);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);
				
				p3+=6;
				int p4=str.indexOf("order");
				String wherearr=str.substring(p3,p4-1);
				String[] wherea1=wherearr.split(",");
				wherelist=where_list(wherea1);
				Node wherenode=par1.expr_wherelist(wherelist);
				
				p4+=9; //order by һ���Ÿ��ַ� 
				String orderarr=str.substring(p4); //ȡ��order by����ѡ�����ֶ�
				String[] ordera1=orderarr.split(",");
				orderlist=order_list(ordera1);
				Node ordernode=par1.expr_orderby(orderlist);
				
				List<Object> list=new ArrayList<>();
				list.add("select");
				list.add("distinct");
				list.add(selnode);
				list.add("from");
				list.add(fromnode);
				list.add("where");
				list.add(wherenode);
				list.add("orderby");
				list.add(ordernode);
				Node tree=par1.expr_sdfwo(list);
				Node.global_syn_tree=tree;
				return tree;
			}
			else if(pos==1){ //��Ӧ��SFWOģʽ
				//System.out.println("������SFWOģʽ");
				int p1=str.indexOf("select");
				p1+=7; //�ҵ�ѡ������Կ�ʼ��λ��
				int p2=str.indexOf("from"); //�ҵ�from��λ��
				String selarr=str.substring(p1,p2-1); //ѡ�������ַ�������ѡ�����ֶβ���
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				p2+=5;
				int p3=str.indexOf("where");
				String fromarr=str.substring(p2,p3-1);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);
				p3+=6;
				int p4=str.indexOf("order");
				String wherearr=str.substring(p3,p4-1);
				String[] wherea1=wherearr.split(",");
				wherelist=where_list(wherea1);
				Node wherenode=par1.expr_wherelist(wherelist);
				p4+=9; //order by һ���Ÿ��ַ� 
				String orderarr=str.substring(p4); //ȡ��order by����ѡ�����ֶ�
				String[] ordera1=orderarr.split(",");
				orderlist=order_list(ordera1);
				Node ordernode=par1.expr_orderby(orderlist);
				
				List<Object> list=new ArrayList<>();
				list.add("select");
				list.add(selnode);
				list.add("from");
				list.add(fromnode);
				list.add("where");
				list.add(wherenode);
				list.add("orderby");
				list.add(ordernode);
				Node tree=par1.expr_sfwo(list);
				Node.global_syn_tree=tree;
				return tree;
			}
			else if(pos==2){ //��ӦSDFOģʽ
				//System.out.println("������SDFOģʽ");
				int p1=str.indexOf("distinct");
				p1+=9; //�ҵ�ѡ������Կ�ʼ��λ��
				int p2=str.indexOf("from"); //�ҵ�from��λ��
				String selarr=str.substring(p1,p2-1); //ѡ�������ַ�������ѡ�����ֶβ���
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				p2+=5;
				int p3=str.indexOf("order");
				String fromarr=str.substring(p2,p3-1);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);
				p3+=9;
				String orderarr=str.substring(p3); //ȡ��order by����ѡ�����ֶ�
				String[] ordera1=orderarr.split(",");
				orderlist=order_list(ordera1);
				Node ordernode=par1.expr_orderby(orderlist);
				
				List<Object> list=new ArrayList<>();
				list.add("select");
				list.add("distinct");
				list.add(selnode);
				list.add("from");
				list.add(fromnode);
				list.add("orderby");
				list.add(ordernode);
				Node tree=par1.expr_sdfo(list);
				Node.global_syn_tree=tree;
				return tree;
			}
			else if(pos==3){ //��ӦSDFWģʽ
				//System.out.println("������SDFWģʽ");
				int p1=str.indexOf("distinct");
				p1+=9; //�ҵ�ѡ������Կ�ʼ��λ��
				int p2=str.indexOf("from"); //�ҵ�from��λ��
				String selarr=str.substring(p1,p2-1); //ѡ�������ַ�������ѡ�����ֶβ���
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				p2+=5;
				int p3=str.indexOf("where");
				String fromarr=str.substring(p2,p3-1);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);
				p3+=6;
				String wherearr=str.substring(p3); //ȡ��order by����ѡ�����ֶ�
				String[] wherea1=wherearr.split(",");
				wherelist=order_list(wherea1);
				Node wherenode=par1.expr_wherelist(wherelist);
				
				List<Object> list=new ArrayList<>();
				list.add("select");
				list.add("distinct");
				list.add(selnode);
				list.add("from");
				list.add(fromnode);
				list.add("where");
				list.add(wherenode);
				Node tree=par1.expr_sdfw(list);
				Node.global_syn_tree=tree;
				return tree;
			}
			else if(pos==4){ //��ӦSFOģʽ
				//System.out.println("������SFOģʽ");
				int p1=str.indexOf("select");
				p1+=7; //�ҵ�ѡ������Կ�ʼ��λ��
				int p2=str.indexOf("from"); //�ҵ�from��λ��
				String selarr=str.substring(p1,p2-1); //ѡ�������ַ�������ѡ�����ֶβ���
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				p2+=5;
				int p3=str.indexOf("order");
				String fromarr=str.substring(p2,p3-1);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);	
				p3+=9; //order by һ���Ÿ��ַ� 
				String orderarr=str.substring(p3); //ȡ��order by����ѡ�����ֶ�
				String[] ordera1=orderarr.split(",");
				orderlist=order_list(ordera1);
				Node ordernode=par1.expr_orderby(orderlist);
				
				List<Object> list=new ArrayList<>();
				list.add("select");
				list.add(selnode);
				list.add("from");
				list.add(fromnode);
				list.add("orderby");
				list.add(ordernode);
				Node tree=par1.expr_sfo(list);
				Node.global_syn_tree=tree;
				return tree;
			}
			else if(pos==5){ //��ӦSFWģʽ
				//System.out.println("������SFWģʽ");
				int p1=str.indexOf("select");
				p1+=7; //�ҵ�ѡ������Կ�ʼ��λ��
				int p2=str.indexOf("from"); //�ҵ�from��λ��
				String selarr=str.substring(p1,p2-1); //ѡ�������ַ�������ѡ�����ֶβ���
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				p2+=5;
				int p3=str.indexOf("where");
				String fromarr=str.substring(p2,p3-1);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);
				p3+=6;
				String wherearr=str.substring(p3);
				String[] wherea1=wherearr.split(",");
				wherelist=where_list(wherea1);
				Node wherenode=par1.expr_wherelist(wherelist);
				
				List<Object> list=new ArrayList<>();
				list.add("select");
				list.add(selnode);
				list.add("from");
				list.add(fromnode);
				list.add("where");
				list.add(wherenode);
				Node tree=par1.expr_sfw(list);
				Node.global_syn_tree=tree;
				return tree;
			}
			else if(pos==6){ //��ӦSDFģʽ
				//System.out.println("������SDFģʽ");
				int p1=str.indexOf("distinct");
				p1+=9; //�ҵ�ѡ������Կ�ʼ��λ��
				int p2=str.indexOf("from"); //�ҵ�from��λ��
				String selarr=str.substring(p1,p2-1); //ѡ�������ַ�������ѡ�����ֶβ���
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				p2+=5;
				String fromarr=str.substring(p2);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);
				
				List<Object> list=new ArrayList<>();
				list.add("select");
				list.add("distinct");
				list.add(selnode);
				list.add("from");
				list.add(fromnode);
				Node tree=par1.expr_sdf(list);
				Node.global_syn_tree=tree;
				return tree;
			}
			else { //��ӦSFģʽ
				//System.out.println("������SFģʽ");
				int p1=str.indexOf("select");
				p1+=7; //�ҵ�ѡ������Կ�ʼ��λ��
				int p2=str.indexOf("from"); //�ҵ�from��λ��
				String selarr=str.substring(p1,p2-1); //ѡ�������ַ�������ѡ�����ֶβ���
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				p2+=5;
				String fromarr=str.substring(p2);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);
				
				List<Object> list=new ArrayList<>();
				list.add("select");
				list.add(selnode);
				list.add("from");
				list.add(fromnode);
				Node tree=par1.expr_sf(list);
				Node.global_syn_tree=tree;
				return tree;
			}
		}
		else{
			System.out.println("��SQL��ѯ����﷨���󣡲������������Ӧ��ģʽ");
			/*Node node=new Node("",null,null);
			return node;*/
			Node.global_logical_tree=null;
			return null;
		}
	}
}
