package xmj.minibase01;

import java.util.ArrayList;
import java.util.List;

public class Parse_db {
	public Node expr_sellist(List<Object> t){
		//System.out.println("t�ĳ���Ϊ"+t.size());
		if(t.size()==1){
			Node seln1=new Node((String)t.get(0),null,null);//��Ψһ���ֶ�����һ��Ҷ�ڵ�
			List<Node> list1=new ArrayList<>();
			list1.add(seln1); //��Ϊ���ӽڵ�
			Node selattr=new Node("TCNAME",list1,null); //����TCNAME�ڵ�
			list1=new ArrayList<>();
			list1.add(selattr);//��TCNAME�ڵ���Ϊ���ӽڵ�
			Node selnode=new Node("SelList",list1,null);//����SelList�ڵ�
			//selnode.show(selnode);
			return selnode;
		}
		else{ //ѡ���˲�ֹһ���ֶ�
			Node seln1=new Node((String)t.get(0),null,null);
			List<Node> li1=new ArrayList<>();
			li1.add(seln1);
			Node seln11=new Node("TCNAME",li1,null); //���ɵ�һ���ֶζ�Ӧ�Ľڵ�
			
			Node seln2=new Node((String)t.get(1),null,null);
			
			List<Object> list2=(List)t.get(2);
			Node seln3=expr_sellist(list2); //��ݹ�����������ֶζ�Ӧ�ڵ�
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
			Node fromn1=new Node((String)t.get(0),null,null);//��Ψһ�ı�������һ��Ҷ�ڵ�
			List<Node> list1=new ArrayList<>();
			list1.add(fromn1); //��Ϊ���ӽڵ�
			Node fromattr=new Node("TCNAME",list1,null); //����TCNAME�ڵ�
			list1=new ArrayList<>();
			list1.add(fromattr);//��TCNAME�ڵ���Ϊ���ӽڵ�
			Node fromnode=new Node("FromList",list1,null);//����SelList�ڵ�
			return fromnode;
		}
		else{ //ѡ���˲�ֹһ���ֶ�
			Node fromn1=new Node((String)t.get(0),null,null);
			List<Node> li1=new ArrayList<>();
			li1.add(fromn1);
			Node fromn11=new Node("TCNAME",li1,null); //���ɵ�һ���ֶζ�Ӧ�Ľڵ�
			
			Node fromn2=new Node((String)t.get(1),null,null);
			
			List<Object> list2=(List)t.get(2);
			Node fromn3=expr_fromlist(list2); //��ݹ�����������ֶζ�Ӧ�ڵ�
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
		if(t.size()==1){//ֻ��һ������
			List<Object> o1=(List)t.get(0);
			Node wheren1=new Node((String)o1.get(0),null,null);
			List<Node> li0=new ArrayList<>();
			li0.add(wheren1);
			Node wheren11=new Node("TCNAME",li0,null);
			Node wheren2=new Node((String)o1.get(1),null,null);
			Node wheren3=new Node((String)o1.get(2),null,null);
			List<Node> li1=new ArrayList<>();
			li1.add(wheren3);
			List<Node> li2=new ArrayList<>(); //��cond�ĺ��ӽڵ��б�
			li2.add(wheren11);
			li2.add(wheren2);
			String ch1=(String)o1.get(2);
			if(ch1.contains(".")){//�ǵ�ֵ��������
				Node wheren33=new Node("TCNAME",li1,null);
				li2.add(wheren33);
			}
			else{ //��ͨ������ж�����
				Node wheren33=new Node("CONSTRAINT",li1,null);
				li2.add(wheren33);
			}
			Node condnode=new Node("Cond",li2,null);
			List<Node> li3=new ArrayList<>();
			li3.add(condnode);
			Node wherenode=new Node("WhereList",li3,null);
			return wherenode;
		}
		else{ //�в�ֹһ������������������
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
				List<Node> li2=new ArrayList<>(); //��cond�ĺ��ӽڵ��б�
				li2.add(wheren11);
				li2.add(wheren2);
				String ch1=(String)condition.get(2);
				if(ch1.contains(".")){//�ǵ�ֵ��������
					Node wheren33=new Node("TCNAME",li1,null);
					li2.add(wheren33);
				}
				else{ //��ͨ������ж�����
					Node wheren33=new Node("CONSTRAINT",li1,null);
					li2.add(wheren33);
				}
				Node condnode=new Node("Cond",li2,null);
				li3.add(condnode);
				if(i!=t.size()-1){ //�������һ����������Ҫ��һ��AND
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
			if(o1 instanceof String){ //ֻ��һ�����ֶΣ�û��desc����asc
				Node ordern1=new Node((String)o1,null,null);
				List<Node> li1=new ArrayList<>();
				li1.add(ordern1);
				Node ordern11=new Node("TCNAME",li1,null);
				List<Node> li2=new ArrayList<>();
				li2.add(ordern11);
				Node ordernode=new Node("OrderList",li2,null);
				return ordernode;
			}
			else{ //��desc����asc
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
			if(o1 instanceof String){ //û��desc����asc
				Node ordern1=new Node((String)o1,null,null);
				List<Node> li1=new ArrayList<>();
				li1.add(ordern1);
				Node ordern11=new Node("TCNAME",li1,null);
				orderlist1.add(ordern11);
			}
			else{ //��desc����asc
				List<Object> orderli=(List)t.get(0); //��ȡ��һ�������ֶ�
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
			orderlist1.add(comma); //���붺�Žڵ�
			List<Object> list2=(List)t.get(2); //���������ֶε��б�
			Node ordern4=expr_orderby(list2); //�ݹ���ú�����������ֶζ�Ӧ�Ľڵ�
			orderlist1.add(ordern4);
			Node ordernode=new Node("OrderList",orderlist1,null);
			return ordernode;
		}
	}
	public Node expr_sfw(List<Object> t){
		//ƥ��select sellist from fromlist where wherelist 
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
		//ƥ��select sellist from fromlist where wherelist order by orderlist 
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
		//ƥ��select sellist from fromlist order by orderlist 
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
		//ƥ��select sellist from fromlist 
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
		//ƥ��select distinct sellist from fromlist 
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
		//ƥ��select distinct sellist from fromlist order by orderlist 
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
		//ƥ��select distinct sellist from fromlist where wherelist 
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
		//ƥ��select distinct sellist from fromlist where wherelist order by orderlist 
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
