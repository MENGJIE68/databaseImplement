package xmj.minibase01;

import java.util.ArrayList;
import java.util.List;

//现在以及将输入的sql语句匹配到了对应的模式，并处理了对应的sellist,fromlist.wherelist.orderlist
//下面要处理的是将他对应到各个函数里去，构造语法树，模仿parse_db.py，直接在各个分支里调用对应的函数即可
import java.util.regex.Pattern;

//进度：换成正则表达式去匹配各个模式而不是单纯的用空格分开然后使用长度判断模式
//where部分条件和前面不一样，一个条件是..=..，或者.. like ..
//最长最短匹配
public class Judge {
	public List<Object> sellist;
	public List<Object> fromlist;
	public List<Object> wherelist;
	public List<Object> orderlist;
	
	public List<Object> sel_list(String[] sela1){
		if(sela1.length==1){
			//System.out.println("长度为1");
			List<Object> list1=new ArrayList<>();
			list1.add(sela1[0]);
			return list1;
		}
		else {
			//System.out.println("长度大于1");
			List<Object> sellist=new ArrayList<>();
			sellist.add(sela1[0]);
			sellist.add(",");
			String[] sela2=new String[sela1.length-1];
			for(int i=0;i<sela2.length;i++){
				sela2[i]=sela1[i+1];
			}
			List<Object> list2=sel_list(sela2); //递归生成后续字段的列表
			sellist.add(list2); //将其他的选择字段作为第三个元素（是个列表）
			//System.out.println(sellist);
			return sellist;
		}
	}
	public List<Object> from_list(String[] froma1){
		if(froma1.length==1){
			//System.out.println("长度为1");
			List<Object> list1=new ArrayList<>();
			list1.add(froma1[0]);
			return list1;
		}
		else {
			//System.out.println("长度大于1");
			List<Object> fromlist=new ArrayList<>();
			fromlist.add(froma1[0]);
			fromlist.add(",");
			String[] froma2=new String[froma1.length-1];
			for(int i=0;i<froma2.length;i++){
				froma2[i]=froma1[i+1];
			}
			List<Object> list2=from_list(froma2); //递归生成后续表名的列表
			fromlist.add(list2);
			//System.out.println(fromlist);
			return fromlist;
		}
	}
	public List<Object> where_list(String[] wherea1){ //wherea1里面的每个元素是一个条件
		List<Object> wherelist=new ArrayList<>();
		for(int i=0;i<wherea1.length;i++){ //遍历处理每一个条件
			String wherestr=wherea1[i];
			List<String> list1=new ArrayList<>();
			if(wherestr.matches(".*=.*")){
				//是等值或者小于大于条件
				int pos1=wherestr.indexOf("=");
				String attr=wherestr.substring(0, pos1); //获取等值条件对应的字段部分
				String ch=wherestr.substring(pos1+1); //获取等值条件对应的常量部分
				list1.add(attr);
				list1.add("=");
				list1.add(ch);
			}
			else if(wherestr.matches(".*like.*")){
				int pos1=wherestr.indexOf("like");
				String attr=wherestr.substring(0, pos1); //获取等值条件对应的字段部分
				String ch=wherestr.substring(pos1+5); //获取等值条件对应的常量部分
				list1.add(attr);
				list1.add("like");
				list1.add(ch);
			}
			else if(wherestr.matches(".*>.*")){
				int pos1=wherestr.indexOf(">");
				String attr=wherestr.substring(0, pos1); //获取等值条件对应的字段部分
				String ch=wherestr.substring(pos1+1); //获取等值条件对应的常量部分
				list1.add(attr);
				list1.add(">");
				list1.add(ch);
			}
			else if(wherestr.matches(".*<.*")){
				int pos1=wherestr.indexOf("<");
				String attr=wherestr.substring(0, pos1); //获取等值条件对应的字段部分
				String ch=wherestr.substring(pos1+1); //获取等值条件对应的常量部分
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
	public List<Object> order_list(String[] ordera1){ //每个元素为  '属性1'  或者   '属性1 desc/asc'
		if(ordera1.length==1){
			//System.out.println("长度为1");
			List<Object> list1=new ArrayList<>();
			if((ordera1[0].matches(".*\\sdesc"))||(ordera1[0].matches(".*\\sasc"))){
				//有desc或者asc约束
				String[] arr=ordera1[0].split(" ");
				List<String> subli=new ArrayList<>();
				subli.add(arr[0]);
				subli.add(arr[1]);
				list1.add(subli);
			}
			else{ //没有desc或者asc约束
				list1.add(ordera1[0]);
			}
			return list1;
		}
		else {
			//System.out.println("长度大于1");
			List<Object> orderlist=new ArrayList<>();
			if((ordera1[0].matches(".*\\sdesc"))||(ordera1[0].matches(".*\\sasc"))){
				//有desc或者asc约束
				String[] arr=ordera1[0].split(" ");
				List<String> subli=new ArrayList<>();
				subli.add(arr[0]);
				subli.add(arr[1]);
				orderlist.add(subli);
			}
			else{ //没有desc或者asc约束
				orderlist.add(ordera1[0]);
			}
			
			orderlist.add(",");
			String[] ordera2=new String[ordera1.length-1];
			for(int i=0;i<ordera2.length;i++){
				ordera2[i]=ordera1[i+1];
			}
			List<Object> list2=order_list(ordera2);
			orderlist.add(list2); //将其他的选择字段作为第三个元素（是个列表）
			//System.out.println(orderlist);
			return orderlist;
		}
	}
	public Node judge(String str){	
		System.out.println("这条语句为"+str);
		List<String> patlist=new ArrayList<>(); //模式列表
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
				System.out.println("现在匹配的模式为"+patlist.get(i));
				break;
			}
		}
		if(pos!=-1){ //这个sql语句有匹配的模式
			Parse_db par1=new Parse_db();
			if(pos==0){ //对应的SDFWO模式
				//System.out.println("现在是SDFWO模式");
				int p1=str.indexOf("distinct");
				p1+=9; //找到选择的属性开始的位置
				int p2=str.indexOf("from"); //找到from的位置
				String selarr=str.substring(p1,p2-1); //选出的子字符串就是选出的字段部分
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
				
				p4+=9; //order by 一共九个字符 
				String orderarr=str.substring(p4); //取得order by后面选出的字段
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
			else if(pos==1){ //对应的SFWO模式
				//System.out.println("现在是SFWO模式");
				int p1=str.indexOf("select");
				p1+=7; //找到选择的属性开始的位置
				int p2=str.indexOf("from"); //找到from的位置
				String selarr=str.substring(p1,p2-1); //选出的子字符串就是选出的字段部分
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
				p4+=9; //order by 一共九个字符 
				String orderarr=str.substring(p4); //取得order by后面选出的字段
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
			else if(pos==2){ //对应SDFO模式
				//System.out.println("现在是SDFO模式");
				int p1=str.indexOf("distinct");
				p1+=9; //找到选择的属性开始的位置
				int p2=str.indexOf("from"); //找到from的位置
				String selarr=str.substring(p1,p2-1); //选出的子字符串就是选出的字段部分
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
				String orderarr=str.substring(p3); //取得order by后面选出的字段
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
			else if(pos==3){ //对应SDFW模式
				//System.out.println("现在是SDFW模式");
				int p1=str.indexOf("distinct");
				p1+=9; //找到选择的属性开始的位置
				int p2=str.indexOf("from"); //找到from的位置
				String selarr=str.substring(p1,p2-1); //选出的子字符串就是选出的字段部分
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
				String wherearr=str.substring(p3); //取得order by后面选出的字段
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
			else if(pos==4){ //对应SFO模式
				//System.out.println("现在是SFO模式");
				int p1=str.indexOf("select");
				p1+=7; //找到选择的属性开始的位置
				int p2=str.indexOf("from"); //找到from的位置
				String selarr=str.substring(p1,p2-1); //选出的子字符串就是选出的字段部分
				String[] sela1=selarr.split(",");
				sellist=sel_list(sela1);
				Node selnode=par1.expr_sellist(sellist);
				p2+=5;
				int p3=str.indexOf("order");
				String fromarr=str.substring(p2,p3-1);
				String[] froma1=fromarr.split(",");
				fromlist=from_list(froma1);
				Node fromnode=par1.expr_fromlist(fromlist);	
				p3+=9; //order by 一共九个字符 
				String orderarr=str.substring(p3); //取得order by后面选出的字段
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
			else if(pos==5){ //对应SFW模式
				//System.out.println("现在是SFW模式");
				int p1=str.indexOf("select");
				p1+=7; //找到选择的属性开始的位置
				int p2=str.indexOf("from"); //找到from的位置
				String selarr=str.substring(p1,p2-1); //选出的子字符串就是选出的字段部分
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
			else if(pos==6){ //对应SDF模式
				//System.out.println("现在是SDF模式");
				int p1=str.indexOf("distinct");
				p1+=9; //找到选择的属性开始的位置
				int p2=str.indexOf("from"); //找到from的位置
				String selarr=str.substring(p1,p2-1); //选出的子字符串就是选出的字段部分
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
			else { //对应SF模式
				//System.out.println("现在是SF模式");
				int p1=str.indexOf("select");
				p1+=7; //找到选择的属性开始的位置
				int p2=str.indexOf("from"); //找到from的位置
				String selarr=str.substring(p1,p2-1); //选出的子字符串就是选出的字段部分
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
			System.out.println("该SQL查询语句语法错误！不存在这个语句对应的模式");
			/*Node node=new Node("",null,null);
			return node;*/
			Node.global_logical_tree=null;
			return null;
		}
	}
}
