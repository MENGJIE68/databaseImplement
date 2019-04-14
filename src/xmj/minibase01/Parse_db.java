package xmj.minibase01;

import java.util.ArrayList;
import java.util.List;

public class Parse_db {
	public Node expr_sellist(List<Object> t){
		//System.out.println("t的长度为"+t.size());
		if(t.size()==1){
			Node seln1=new Node((String)t.get(0),null,null);//以唯一的字段生成一个叶节点
			List<Node> list1=new ArrayList<>();
			list1.add(seln1); //作为孩子节点
			Node selattr=new Node("TCNAME",list1,null); //生成TCNAME节点
			list1=new ArrayList<>();
			list1.add(selattr);//将TCNAME节点作为孩子节点
			Node selnode=new Node("SelList",list1,null);//生成SelList节点
			//selnode.show(selnode);
			return selnode;
		}
		else{ //选择了不止一个字段
			Node seln1=new Node((String)t.get(0),null,null);
			List<Node> li1=new ArrayList<>();
			li1.add(seln1);
			Node seln11=new Node("TCNAME",li1,null); //生成第一个字段对应的节点
			
			Node seln2=new Node((String)t.get(1),null,null);
			
			List<Object> list2=(List)t.get(2);
			Node seln3=expr_sellist(list2); //会递归生成其余的字段对应节点
			List<Node> selat=new ArrayList<>();
			selat.add(seln11);
			selat.add(seln2);
			selat.add(seln3);
			Node selnode=new Node("SelList",selat,null);
			//selnode.show(selnode);
			return selnode;
		}
	}
	public Node expr_fromlist(List<Object> t){
		if(t.size()==1){
			Node fromn1=new Node((String)t.get(0),null,null);//以唯一的表名生成一个叶节点
			List<Node> list1=new ArrayList<>();
			list1.add(fromn1); //作为孩子节点
			Node fromattr=new Node("TCNAME",list1,null); //生成TCNAME节点
			list1=new ArrayList<>();
			list1.add(fromattr);//将TCNAME节点作为孩子节点
			Node fromnode=new Node("FromList",list1,null);//生成SelList节点
			return fromnode;
		}
		else{ //选择了不止一个字段
			Node fromn1=new Node((String)t.get(0),null,null);
			List<Node> li1=new ArrayList<>();
			li1.add(fromn1);
			Node fromn11=new Node("TCNAME",li1,null); //生成第一个字段对应的节点
			
			Node fromn2=new Node((String)t.get(1),null,null);
			
			List<Object> list2=(List)t.get(2);
			Node fromn3=expr_fromlist(list2); //会递归生成其余的字段对应节点
			List<Node> fromat=new ArrayList<>();
			fromat.add(fromn11);
			fromat.add(fromn2);
			fromat.add(fromn3);
			Node fromnode=new Node("FromList",fromat,null);
			return fromnode;
		}
	}
	public Node expr_wherelist(List<Object> t){
		System.out.println(t);
		if(t.size()==1){//只有一个条件
			List<Object> o1=(List)t.get(0);
			Node wheren1=new Node((String)o1.get(0),null,null);
			List<Node> li0=new ArrayList<>();
			li0.add(wheren1);
			Node wheren11=new Node("TCNAME",li0,null);
			Node wheren2=new Node((String)o1.get(1),null,null);
			Node wheren3=new Node((String)o1.get(2),null,null);
			List<Node> li1=new ArrayList<>();
			li1.add(wheren3);
			List<Node> li2=new ArrayList<>(); //是cond的孩子节点列表
			li2.add(wheren11);
			li2.add(wheren2);
			String ch1=(String)o1.get(2);
			if(ch1.contains(".")){//是等值连接条件
				Node wheren33=new Node("TCNAME",li1,null);
				li2.add(wheren33);
			}
			else{ //普通的相等判断条件
				Node wheren33=new Node("CONSTRAINT",li1,null);
				li2.add(wheren33);
			}
			Node condnode=new Node("Cond",li2,null);
			List<Node> li3=new ArrayList<>();
			li3.add(condnode);
			Node wherenode=new Node("WhereList",li3,null);
			return wherenode;
		}
		else{ //有不止一个条件（两个条件）
			List<Node> li3=new ArrayList<>();
			for(int i=0;i<t.size();){
				List<String> condition=(List)t.get(i);
				Node wheren1=new Node(condition.get(0),null,null);
				List<Node> li0=new ArrayList<>();
				li0.add(wheren1);
				Node wheren11=new Node("TCNAME",li0,null);
				Node wheren2=new Node(condition.get(1),null,null);
				Node wheren3=new Node(condition.get(2),null,null);
				List<Node> li1=new ArrayList<>();
				li1.add(wheren3);
				List<Node> li2=new ArrayList<>(); //是cond的孩子节点列表
				li2.add(wheren11);
				li2.add(wheren2);
				String ch1=(String)condition.get(2);
				if(ch1.contains(".")){//是等值连接条件
					Node wheren33=new Node("TCNAME",li1,null);
					li2.add(wheren33);
				}
				else{ //普通的相等判断条件
					Node wheren33=new Node("CONSTRAINT",li1,null);
					li2.add(wheren33);
				}
				Node condnode=new Node("Cond",li2,null);
				li3.add(condnode);
				if(i!=t.size()-1){ //不是最后一个条件，则要加一个AND
					Node and=new Node("AND",null,null);
					li3.add(and);
				}
				i+=2;
			}
			Node wherenode=new Node("WhereList",li3,null);
			return wherenode;
		}
	}
	public Node expr_orderby(List<Object> t){
		if(t.size()==1){
			Object o1=t.get(0);
			if(o1 instanceof String){ //只有一排序字段，没有desc或者asc
				Node ordern1=new Node((String)o1,null,null);
				List<Node> li1=new ArrayList<>();
				li1.add(ordern1);
				Node ordern11=new Node("TCNAME",li1,null);
				List<Node> li2=new ArrayList<>();
				li2.add(ordern11);
				Node ordernode=new Node("OrderList",li2,null);
				return ordernode;
			}
			else{ //有desc或者asc
				List<Object> orderli1=(List)t.get(0);
				Node ordern1=new Node((String)orderli1.get(0),null,null);
				List<Node> li1=new ArrayList<>();
				li1.add(ordern1);
				Node ordern11=new Node("TCNAME",li1,null);
				Node ordern2=new Node((String)orderli1.get(1),null,null);
				List<Node> li2=new ArrayList<>();
				li2.add(ordern11);
				li2.add(ordern2);
				Node ordern3=new Node("Exporder",li2,null);
				List<Node> li3=new ArrayList<>();
				li3.add(ordern3);
				Node ordernode=new Node("OrderList",li3,null);
				return ordernode;
			}
		}
		else{
			Object o1=t.get(0);
			List<Node> orderlist1=new ArrayList<>();
			if(o1 instanceof String){ //没有desc或者asc
				Node ordern1=new Node((String)o1,null,null);
				List<Node> li1=new ArrayList<>();
				li1.add(ordern1);
				Node ordern11=new Node("TCNAME",li1,null);
				orderlist1.add(ordern11);
			}
			else{ //有desc或者asc
				List<Object> orderli=(List)t.get(0); //获取第一个排序字段
				Node ordern1=new Node((String)orderli.get(0),null,null);
				List<Node> li1=new ArrayList<>();
				li1.add(ordern1);
				Node ordern11=new Node("TCNAME",li1,null);
				Node ordern2=new Node((String)orderli.get(1),null,null);
				List<Node> li2=new ArrayList<>();
				li2.add(ordern11);
				li2.add(ordern2);
				Node ordern3=new Node("Exporder",li2,null);
				orderlist1.add(ordern3);
			}
			Node comma=new Node(",",null,null);
			orderlist1.add(comma); //加入逗号节点
			List<Object> list2=(List)t.get(2); //后续排序字段的列表
			Node ordern4=expr_orderby(list2); //递归调用函数求出后续字段对应的节点
			orderlist1.add(ordern4);
			Node ordernode=new Node("OrderList",orderlist1,null);
			return ordernode;
		}
	}
	public Node expr_sfw(List<Object> t){
		//匹配select sellist from fromlist where wherelist 
		List<Node> list1=new ArrayList<>();
		Node sel=new Node("SELECT" ,null,null);
		Node from=new Node("FROM",null,null);
		Node where=new Node("WHERE",null,null);
		list1.add(sel);
		list1.add((Node)t.get(1));
		list1.add(from);
		list1.add((Node)t.get(3));
		list1.add(where);
		list1.add((Node)t.get(5));
		Node sfwnode=new Node("SFW",list1,null);
		return sfwnode;
	}
	public Node expr_sfwo(List<Object> t){
		//匹配select sellist from fromlist where wherelist order by orderlist 
		List<Node> list1 = new ArrayList<>();
		Node sel = new Node("SELECT", null, null);
		Node from = new Node("FROM", null, null);
		Node where = new Node("WHERE", null, null);
		Node order = new Node("ORDERBY", null, null);
		list1.add(sel);
		list1.add((Node) t.get(1));
		list1.add(from);
		list1.add((Node) t.get(3));
		list1.add(where);
		list1.add((Node) t.get(5));
		list1.add(order);
		list1.add((Node) t.get(7));
		Node sfwonode = new Node("SFWO", list1, null);
		return sfwonode;
	}
	public Node expr_sfo(List<Object> t){
		//匹配select sellist from fromlist order by orderlist 
		List<Node> list1 = new ArrayList<>();
		Node sel = new Node("SELECT", null, null);
		Node from = new Node("FROM", null, null);
		Node order = new Node("ORDERBY", null, null);
		list1.add(sel);
		list1.add((Node) t.get(1));
		list1.add(from);
		list1.add((Node) t.get(3));
		list1.add(order);
		list1.add((Node) t.get(5));
		Node sfonode = new Node("SFO", list1, null);
		return sfonode;
	}
	public Node expr_sf(List<Object> t){
		//匹配select sellist from fromlist 
		List<Node> list1 = new ArrayList<>();
		Node sel = new Node("SELECT", null, null);
		Node from = new Node("FROM", null, null);
		list1.add(sel);
		list1.add((Node) t.get(1));
		list1.add(from);
		list1.add((Node) t.get(3));
		Node sfnode = new Node("SF", list1, null);
		return sfnode;
	}
	public Node expr_sdf(List<Object> t){
		//匹配select distinct sellist from fromlist 
		List<Node> list1 = new ArrayList<>();
		Node sel = new Node("SELECT", null, null);
		Node dis=new Node("DISTINCT",null,null);
		Node from = new Node("FROM", null, null);
		list1.add(sel);
		list1.add(dis);
		list1.add((Node) t.get(2));
		list1.add(from);
		list1.add((Node) t.get(4));
		Node sdfnode = new Node("SDF", list1, null);
		return sdfnode;
	}
	public Node expr_sdfo(List<Object> t){
		//匹配select distinct sellist from fromlist order by orderlist 
		List<Node> list1 = new ArrayList<>();
		Node sel = new Node("SELECT", null, null);
		Node dis = new Node("DISTINCT", null, null);
		Node from = new Node("FROM", null, null);
		Node order = new Node("ORDERBY", null, null);
		list1.add(sel);
		list1.add(dis);
		list1.add((Node) t.get(2));
		list1.add(from);
		list1.add((Node) t.get(4));
		list1.add(order);
		list1.add((Node) t.get(6));
		Node sdfonode = new Node("SDFO", list1, null);
		return sdfonode;
	}
	public Node expr_sdfw(List<Object> t){
		//匹配select distinct sellist from fromlist where wherelist 
		List<Node> list1 = new ArrayList<>();
		Node sel = new Node("SELECT", null, null);
		Node dis=new Node("DISTINCT",null,null);
		Node from = new Node("FROM", null, null);
		Node where = new Node("WHERE", null, null);
		list1.add(sel);
		list1.add(dis);
		list1.add((Node) t.get(2));
		list1.add(from);
		list1.add((Node) t.get(4));
		list1.add(where);
		list1.add((Node) t.get(6));
		Node sdfwnode = new Node("SDFW", list1, null);
		return sdfwnode;
	}
	public Node expr_sdfwo(List<Object> t){
		//匹配select distinct sellist from fromlist where wherelist order by orderlist 
		List<Node> list1 = new ArrayList<>();
		Node sel = new Node("SELECT", null, null);
		Node dis=new Node("DISTINCT",null,null);
		Node from = new Node("FROM", null, null);
		Node where = new Node("WHERE", null, null);
		Node order = new Node("ORDERBY", null, null);
		list1.add(sel);
		list1.add(dis);
		list1.add((Node) t.get(2));
		list1.add(from);
		list1.add((Node) t.get(4));
		list1.add(where);
		list1.add((Node) t.get(6));
		list1.add(order);
		list1.add((Node) t.get(8));
		Node sdfwonode = new Node("SDFWO", list1, null);
		return sdfwonode;
	}
}
