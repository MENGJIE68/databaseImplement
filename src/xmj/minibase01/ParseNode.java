package xmj.minibase01;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParseNode {
	public List<Object> sel_list;
	public List<Object> from_list;
	public List<Object> where_list;
	public List<Object> order_list;
	public Node syn_tree=Node.global_syn_tree;
	public List<String> outputfield=new ArrayList<>();
	public List<List<String>> current_list=new ArrayList<>();
	public Boolean isright=false;
	
	public ParseNode(){
		sel_list=new ArrayList<>();
		from_list=new ArrayList<>();
		where_list=new ArrayList<>();
		order_list=new ArrayList<>();
	}
	public List<Object> get_sel_list(){
		return this.sel_list;
	}
	public List<Object> get_from_list(){
		return this.from_list;
	}
	public List<Object> get_where_list(){
		return this.where_list;
	}
	public List<Object> get_order_list(){
		return this.order_list;
	}
	public void update_sel_list(List<Object> list){
		this.sel_list=list;
	}
	public void update_from_list(List<Object> list){
		this.from_list=list;
	}
	public void update_where_list(List<Object> list){
		this.where_list=list;
	}
	public void update_order_list(List<Object> list){
		this.order_list=list;
	}
	
	public ParseNode extract_sfw_data(){
		System.out.println("现在开始执行提取sfw数据操作。");
		
		Node syn_tree=Node.global_syn_tree;
		if(syn_tree==null){
			System.out.println("错误！");
			return null;
		}
		else{
			ParseNode PN=new ParseNode();
			destruct(syn_tree,PN);
			return PN;
		}
	}
	//tmplist里面保存的是nodeobj的value值 ，其实应该就是需要找出的每个sellist,fromlist,wherelist等里面的属性、关系名等
	public void show(Node nodeobj,List<Object> tmplist){
		if(nodeobj.children==null){
			Object o1=nodeobj.value;
			tmplist.add(o1);
		}
		else{
			for(int i=0;i<nodeobj.children.size();i++){
				show(nodeobj.children.get(i),tmplist);
			}
		}
	}
	//从语法树里获得sellist,fromlist,wherelist,orderlist
	public void destruct(Node nodeobj,ParseNode PN){
		if(nodeobj.children!=null){
			if(nodeobj.value=="SelList"){
				List<Object> tmplist=new ArrayList<>();
				show(nodeobj,tmplist);
				PN.update_sel_list(tmplist);
			}
			else if(nodeobj.value=="FromList"){
				List<Object> tmplist=new ArrayList<>();
				show(nodeobj,tmplist);
				PN.update_from_list(tmplist);
			}
			else if(nodeobj.value=="WhereList"){
				List<Object> tmplist=new ArrayList<>();
				show(nodeobj,tmplist);
				PN.update_where_list(tmplist);
			}
			else if(nodeobj.value=="OrderList"){
				List<Object> tmplist=new ArrayList<>();
				show(nodeobj,tmplist);
				PN.update_order_list(tmplist);
			}
			else{
				for(int i=0;i<nodeobj.children.size();i++){
					destruct(nodeobj.children.get(i),PN);
				}
			}
		}
	}
	
	public Node construct_from_node(List<Object> fromlist){
		Node node=new Node();
		if(fromlist.size()!=0){
			if(fromlist.size()==1){
				Node tempnode=new Node((String)fromlist.get(0),null,null); //以表名生成一个叶子节点
				List<Node> templist=new ArrayList<>();
				templist.add(tempnode);
				node=new Node("From",templist,null); //使用刚生成的叶节点作为孩子节点生成父节点
				//return node;
			}
			else if(fromlist.size()==2){
				Node tempnode1=new Node((String)fromlist.get(0),null,null); //使用两个表名分别产生两个叶节点
				Node tempnode2=new Node((String)fromlist.get(1),null,null);
				List<Node> templist=new ArrayList<>();
				templist.add(tempnode1);
				templist.add(tempnode2);
				node=new Node("From",templist,null); //生成父节点
				//return node;
			}
			else if(fromlist.size()>2){
				Node tempnode=new Node((String)fromlist.get(fromlist.size()-1),null,null);
				//，每次选择最后一个元素值去生成一个叶节点
				List<Object> temp=new ArrayList<>();
				for(int i=0;i<fromlist.size()-1;i++){ //去除最后一个元素的from列表
					temp.add(fromlist.get(i));
				}
				List<Node> templist=new ArrayList<>();
				templist.add(construct_from_node(temp)); //递归调用构建from节点函数
				templist.add(tempnode);
				node=new Node("From",templist,null); 
				//return node;
			}
		}
		return node;
		
	}
	
	public Node construct_where_node(Node fromnode,List<Object> wherelist){
		Node node=new Node();
		if((fromnode!=null)&&(wherelist.size()>0)){
			List<Node> templist=new ArrayList<>();
			templist.add(fromnode); //使用from节点作为where节点的孩子
			node=new Node("Where",templist,wherelist);
		}
		else if((fromnode==null)&&(wherelist.size()==0)){
			return fromnode;
		}
		return node;
	}
	
	public Node construct_select_node(Node wf_node,List<Object> sel_list){
		Node node=new Node();
		if((wf_node!=null)&&(sel_list.size()>0)){
			List<Node> templist=new ArrayList<>();
			templist.add(wf_node);
			node=new Node("Sel",templist,sel_list); //使用where节点作为sel节点的孩子
		}
		return node;
	}
	
	public Node construct_distinct_select_node(Node selnode){
		Node node=new Node();
		List<String> sqlsent=new ArrayList<>();
		Node.traver(selnode, sqlsent);
		System.out.println(sqlsent);
		if(!sqlsent.contains("Distinct")){
			return selnode;
		}
		else{
			List<Node> templist=new ArrayList<>();
			templist.add(selnode);
			node=new Node("Distinct",templist,null);
		}
		return node;
	}
	
	public Node construct_order_node(Node distinct_node,List<Object> orderlist){
		Node node=new Node();
		System.out.println(distinct_node!=null);
		System.out.println("orderlistd的长度为："+orderlist.size());
		if((distinct_node!=null)&&(orderlist.size()>0)){
			System.out.println("现在开始执行构建order节点");
			List<Node> templist=new ArrayList<>();
			templist.add(distinct_node);
			node=new Node("Order",templist,orderlist);
		}
		else{
			return distinct_node;
		}
		return node;
	}
	
	//用于构建树的dict,不同idx节点的dict[idx]类型不同
	public void build_execute_tree(Node nodeobj,int idx,HashMap<Integer,List<List<Object>>> dict){
		if(!dict.containsKey(idx)){ //还没有这个键值存在时，要初始化一个空列表
			dict.put(idx, new ArrayList<>()); //初始化为空列表
		}
		List<Object> l1=new ArrayList<>(); 
		System.out.println("这里的nodeobj的value为"+nodeobj.value);
		Object o1=nodeobj.value;
		l1.add(o1); //首先将以value为对象加入列表
		/*List<List<Object>> list1=dict.get(idx);
		list1.add(l1);
		dict.put(idx,list1);*/
		dict.get(idx).add(l1);
		if(nodeobj.var!=null){
			System.out.println("这里的var不为空");
			int len=dict.get(idx).size();
			List<Object> list=new ArrayList<>();
			System.out.println("他的var列表为"+nodeobj.var);
			Object o2=nodeobj.var; //将nodeobj.var转变成object类型
			Object o3=dict.get(idx).get(len-1); //将原来的最后一个元素也转变成object类型
			list.add(o3);
			list.add(o2);
			
			dict.get(idx).remove(len-1); //删除原来的最后一个元素
			dict.get(idx).add(list);
		}
		if(nodeobj.children!=null){
			for(int i=0;i<nodeobj.children.size();i++){
				build_execute_tree(nodeobj.children.get(i),idx+1,dict);
			}
		}
	}
	
	public List<List<String>> dikaerji(List<List<List<String>>> input){
		List<List<String>> a0=input.get(0); //获取笛卡尔积的第一个集合
		List<List<String>> temp=new ArrayList<>();
		for(int i=1;i<input.size();i++){
			List<List<String>> a1=input.get(i);  //获取笛卡尔积的集合里其他的子集合
			
			for(int j=0;j<a0.size();j++){
				for(int k=0;k<a1.size();k++){
					List<List<String>> cut=new ArrayList<>();
					cut.add(a0.get(j));
					cut.add(a1.get(k));
					List<String> re=new ArrayList<>();
					for(int t=0;t<cut.get(0).size();t++){
						re.add(cut.get(0).get(t));
					}
					for(int t=0;t<cut.get(1).size();t++){
						re.add(cut.get(1).get(t));
					}
					temp.add(re);
				}
			}
			a0=temp;
		}
		return temp;
	}
	public List<List<String>> nature_link(List<List<List<String>>> input,List<List<String>> field){
		List<String> f0=field.get(0); //取第一个表的域名列表
		List<List<Integer>> temp_index=new ArrayList<>();
		for(int i=1;i<field.size();i++){
			List<String> f1=field.get(i); //获取遍历的第i个表的域名列表
			for(int j=0;j<f0.size();j++){
				for(int k=0;k<f1.size();k++){
					if(f0.get(j).equals(f1.get(k))){ //如果这两个表的域名相同
						List<Integer> temp=new ArrayList<>();
						temp.add(j);
						temp.add(k);
						temp_index.add(temp); //里面存储的是  [0.j，i.k]，表示第0个表的第j个属性和第i个表的第k个属性相同
					}
				}
			}
		}
		List<List<String>> result=new ArrayList<>(); //保存结果
		if(temp_index.size()!=0){ //如果存在相同的域
			List<List<String>> a0=input.get(0); //获取第一个表的记录列表
			for(int i=1;i<input.size();i++){
				List<List<String>> a1=input.get(i);
				for(int j=0;j<a0.size();j++){ //对第一张表的每个记录
					for(int k=0;k<a1.size();k++){ //对第i张表的每个记录
						List<String> re0=a0.get(j);
						List<String> re1=a1.get(k);
						Boolean flag=true;
						for(int t=0;t<temp_index.size();t++){
							if(!re0.get(temp_index.get(t).get(0)).equals(re1.get(temp_index.get(t).get(1)))){
								//看两个记录的相应相同域名下标处的值是否相同
								flag=false;
								break;
							}
						}
						if(flag==true){ //表示这两个记录在所有的相同字段上的属性取值均相等
							List<String> subre=re0; //两个记录自然连接的结果
							for(int m=0;m<re1.size();m++){
								for(int t=0;t<temp_index.size();t++){
									if(temp_index.get(t).get(1)!=m){
										subre.add(re1.get(m)); //subre里面添加re1的非重复字段
									}
								}
							}
							result.add(subre);
						}
					}
				}
			}
		}
		else{ //没有相同的域，自然连接失败
			result=dikaerji(input);
		}
		return result;
		
	}
	public void execute_tree(List<String> table_name_list,Node nodeobj) throws IOException{
		/*List<String> where1=new ArrayList<>();
		List<String> sel1=new ArrayList<>();
		where1.add("where");
		sel1.add("sel");*/
		 
		int idx=0;
		HashMap<Integer,List<List<Object>>> dict=new HashMap<>();
		build_execute_tree(nodeobj,idx,dict);
		
		Set<Integer> keys=dict.keySet();
		List<Integer> key_list=new ArrayList<Integer>(keys);
		//对key_List降序排列
		Collections.reverse(key_list); //将key_list逆转
		//key_list.sort((o1,o2)->(o2.compareTo(o1)));
		int fromidx=key_list.get(0);		
		idx=key_list.get(0); //初始现在idx应该为最底层from
		//System.out.println("现在的idx为"+idx);
		List<List<String>> current_field=new ArrayList<>();
		List<String> tablename_order=new ArrayList<>();
		while(idx>=0){
			System.out.println("现在的idx为："+idx);
			if(idx==fromidx){//当前是fromlist
				isright=true;
				System.out.println("现在执行 的是from节点");
				System.out.println("---------"+dict.get(idx)+"---------");
				if(dict.get(idx).size()>1){ //如果fromlist长度>1
					for(int i=0;i<dict.get(idx).size();i++){ //验证from_List里面的每个表是否都是合法的（即都是真的存在数据库的表）
						Object table=dict.get(idx).get(i).get(0);
						String tablename=table.toString();
						if(!table_name_list.contains(tablename)){
							System.out.println("不存在"+table+"在表列表里");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
					}
					//下面是生成两个表的数据对象
					Object table1=dict.get(idx).get(0).get(0);
					Object table2=dict.get(idx).get(1).get(0);
					String tablename1=table1.toString();
					String tablename2=table2.toString();
					Storage dataobj1=new Storage(tablename1);
					Storage dataobj2=new Storage(tablename2); 
					
					current_list=new ArrayList<>();
					tablename_order.add(tablename1);
					tablename_order.add(tablename2);
					
					for(int j=0;j<dataobj1.field_name_list.size();j++){ 
						current_field.add(dataobj1.field_name_list.get(j)); //将表1的域名信息加入current_field
						outputfield.add(table1+"."+dataobj1.field_name_list.get(j).get(0).trim()); //元素为 表名.域名
					}
					for(int j=0;j<dataobj2.field_name_list.size();j++){
						current_field.add(dataobj2.field_name_list.get(j));
						outputfield.add(table2+"."+dataobj2.field_name_list.get(j).get(0).trim()); //元素为 表名.域名
					}
					List<List<String>> record1=dataobj1.getrecord();
					List<List<String>> record2=dataobj2.getrecord();
					List<List<List<String>>> list=new ArrayList<>();
					list.add(record1);
					list.add(record2);
					current_list=dikaerji(list); //获取笛卡尔积的结果
					
				}
				else{ //from_list里面只有一个表名，即只查一个表
					
					Object table=dict.get(idx).get(0).get(0); //获取唯一的表名
					String tablename=table.toString();
					Storage dataobj=new Storage(tablename); //新建一个数据对象
					current_list=dataobj.getrecord();
					System.out.println("现在的current_list为"+current_list);
					for(int i=0;i<dataobj.field_name_list.size();i++){
						outputfield.add(dataobj.field_name_list.get(i).get(0).trim());
						
					}
					tablename_order=new ArrayList<>();
					tablename_order.add(tablename);
					current_field=dataobj.field_name_list;
				}
				
			}
			else if((dict.get(idx).get(0).contains("From"))&&(dict.get(idx).size()>1)){
				//只有当有第三个fromlist时才会执行这个分支   [[from,[...]]]      [from,[...]]              [....]
				System.out.println("------"+dict.get(idx)+"---------");
				//System.out.println("------"+dict.get(idx)+"--"+dict.get(idx).get(0)+"--"+dict.get(idx).get(0).get(1)+"----------");
				isright=true;
				Object table=dict.get(idx).get(1).get(0);
				String tablename=table.toString();
				Storage dataobj=new Storage(tablename);
				tablename_order.add(tablename);
				for(int i=0;i<dataobj.field_name_list.size();i++){
					current_field.add(dataobj.field_name_list.get(i));
					outputfield.add(tablename+"."+dataobj.field_name_list.get(i).get(0).trim());
				}
				List<List<String>> templist=new ArrayList<>();
				templist=current_list; //保存下原来的结果
				current_list=new ArrayList<>(); //重新分配，置空
				List<List<List<String>>> temp=new ArrayList<>();
				temp.add(templist);
				temp.add(dataobj.getrecord());
				current_list=dikaerji(temp); //返回前面的笛卡尔积结果和新的第三个表的笛卡尔积的结果
			}
			else if((dict.get(idx).get(0).contains("From"))&&(dict.get(idx).get(0).size()==1)){
				idx-=1;
				continue;
			}
			//else if(idx==1){
			else if(((List)dict.get(idx).get(0).get(0)).contains("Where")){
				System.out.println("现在开始执行where操作");
				//System.out.println("------"+dict.get(idx)+"---------");
				isright=true;
				System.out.println("------"+dict.get(idx)+"--"+dict.get(idx).get(0)+"--"+dict.get(idx).get(0).get(1)+"----------");
				
				List<List<String>> filter=new ArrayList<>();
				filter=(List)dict.get(idx).get(0).get(1); //转换成list
				System.out.println("现在的条件为："+filter);
				for(int i=0;i<filter.size();i++){ //遍历每个条件
					List<String> choice=filter.get(i);
					if("="==choice.get(1)){ //进行的是等值判断
						String ch1=choice.get(0);
						String ch2=choice.get(2);
						if((ch1.contains("."))&&(ch2.contains("."))){ //如果条件里面的字段有.，表示这个字段是两个表的重复字段（即两个表都有这个字段），这是等值连接
							//那么需要在一条记录里面删除重复字段值
							System.out.println("现在是等值连接判断");
							if((!outputfield.contains(ch1))||(!outputfield.contains(ch2))){
								//如果条件里面的字段属性不在表的域名列表里，表示条件错误
								outputfield=new ArrayList<>();
								current_list=new ArrayList<>();
								isright=false;
								return;
								
							}
							int con1=outputfield.indexOf(ch1); //找到第一个条件的下标
							int con2=-1;
							for(int j=outputfield.size()-1;j>=0;j--){
								if(outputfield.get(j).trim().equals(ch2)){
									con2=j;
									break;
								}
							}
							List<List<String>> temp_current_list=new ArrayList<>();
							temp_current_list=current_list; //保存下原来的笛卡尔积
							System.out.println("原来的笛卡尔积为"+temp_current_list);
							current_list=new ArrayList<>(); 
							for(int j=0;j<temp_current_list.size();j++){
								if(temp_current_list.get(j).get(con1).trim().equals(temp_current_list.get(j).get(con2).trim())){//满足等值条件
									List<String> temp_list=new ArrayList<>();
									for(int k=0;k<temp_current_list.get(j).size();k++){
										if(k!=con2){
											temp_list.add(temp_current_list.get(j).get(k));
										}
									}
									current_list.add(temp_list); //将满足等值连接条件的且删除了重复字段的记录插入current_list
									
								}
								
							}
							System.out.println("现在的结果为"+current_list);
							List<String> temp_outputfield=new ArrayList<>();
							
							temp_outputfield=outputfield; //保存下原来的输出字段，因为要将输出字段里的重复字段也删除
							System.out.println("原来的输出域为"+temp_outputfield);
							outputfield=new ArrayList<>();
							for(int j=0;j<temp_outputfield.size();j++){ //将输出字段名中的表名部分和重复的连接字段名删除
								if(j!=con2){
									outputfield.add(temp_outputfield.get(j));
									/*String con=temp_outputfield.get(j);
									String[] cond1=con.split("\\.");
									outputfield.add(cond1[1]); */
								}
							}
							List<List<String>> temp_current_field=new ArrayList<>();
							temp_current_field=current_field;
							current_field=new ArrayList<>();
							for(int j=0;j<temp_current_field.size();j++){
								if(j!=con2){ //将不是第二个重复字段的域名信息加入current_field
									current_field.add(temp_current_field.get(j));
								}
							}
							System.out.println("下面输出的是输出字段");
							System.out.println(outputfield);
							System.out.println("下面输出的是经过where条件删除以后的笛卡尔积结果");
							System.out.println(current_list);
							
						}
						else{// 这不是等值连接条件，是普通的等值判别条件，等号左边的是字段，等号右边的是常量
							System.out.println("现在是等值判别条件");
							System.out.println(current_field);
							int con1=-1;
							for(int j=0;j<current_field.size();j++){
								if(current_field.get(j).get(0).trim().equals(ch1)){ //找到ch1对应的下标
									con1=j;
									break;
								}
							}
							if(con1==-1){ //没有找到这个连接条件
								System.out.println("没有找到该等值条件对应的字段");
								outputfield=new ArrayList<>();
								current_list=new ArrayList<>();
								isright=false;
								return;
							}
							int con_type=Integer.valueOf(current_field.get(con1).get(1)); //获取该字段的类型
							/*if(con_type==1){ //是int类型
								int newch_1=Integer.valueOf(ch2.trim());
							}
							else if(con_type==2){ //是boolean类型
								
							}*/
							List<List<String>> temp_current_list=new ArrayList<>();
							temp_current_list=current_list;
							current_list=new ArrayList<>();
							ch2=ch2.trim(); 
							for(int j=0;j<temp_current_list.size();j++){
								List<String> temprecord=new ArrayList<>();
								temprecord=temp_current_list.get(j); //获取当前要判断的记录
								String ans=temprecord.get(con1).trim(); //获取当前记录的这个ch1字段的值
								if(ans.equals(ch2)){
									current_list.add(temprecord); //当前记录的该字段值和等式右边的常量等，则加入current_list
								}
							}
							
						}
					}
					
					
				}
				//对所有的条件进行处理完成以后，最后对输出域进行处理，将原来的“表名.字段名”改成“字段名”
				List<String> temp_outputfield=new ArrayList<>();
				temp_outputfield=outputfield;
				outputfield=new ArrayList<>();
				for(int j=0;j<temp_outputfield.size();j++){
					String con=temp_outputfield.get(j);
					String[] cond1=con.split("\\.");
					outputfield.add(cond1[1]); 
				}
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Sel")){
				System.out.println("现在开始执行select");
				System.out.println("------"+dict.get(idx)+"---------");
				isright=true;
				//System.out.println("------"+dict.get(idx)+"--"+dict.get(idx).get(0)+"--"+dict.get(idx).get(0).get(1)+"----------");
				List<String> selli=new ArrayList<>();
				selli=(List)dict.get(idx).get(0).get(1);
				if(selli.get(0)!="*"){ //不是选择全部字段
					List<Integer> selindex=new ArrayList<>();
					for(int i=0;i<selli.size();i++){
						if(!outputfield.contains(selli.get(i))){
							System.out.println("选择的字段不存在，错误");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
						selindex.add(outputfield.indexOf(selli.get(i))); //加入选择字段在输出域的下标
						
					}
					outputfield=selli; //将输出域赋值为select语句里的字段列表
					List<List<String>> temp_current_list=new ArrayList<>();
					temp_current_list=current_list;
					current_list=new ArrayList<>();
					for(int i=0;i<temp_current_list.size();i++){
						List<String> tempre=new ArrayList<>();
						for(int j=0;j<selindex.size();j++){
							tempre.add(temp_current_list.get(i).get(selindex.get(j)));
						}
						current_list.add(tempre);
					}
					System.out.println("现在的current_list是："+current_list);
					
				}
				/*if(idx==0){
					return ;
				}*/
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Distinct")){
				System.out.println("------"+dict.get(idx)+"---------");
				//System.out.println("----------"+dict.get(idx)+"--"+dict.get(idx).get(0)+"--"+dict.get(idx).get(0).get(1)+"-------");
				isright=true;
				List<List<String>> temp_current_list=new ArrayList<>();
				temp_current_list=current_list;
				current_list=new ArrayList<>();
				for(int i=0;i<temp_current_list.size();i++){
					if(!current_list.contains(temp_current_list.get(i))){ //没有出现在temp_current_list里面的记录才加入现在的current_list
						current_list.add(temp_current_list.get(i));
					}
				}
				if(idx==0){
					return;
				}
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Order")){
				System.out.println("------"+dict.get(idx)+"---------");
				//System.out.println("----------"+dict.get(idx)+"--"+dict.get(idx).get(0)+"--"+dict.get(idx).get(0).get(1)+"-------");
				isright=true;
				List<List<String>> order_index=new ArrayList<>();
				List<String> orderlist=new ArrayList<>();
				orderlist=(List)dict.get(idx).get(0).get(1);
				System.out.println("现在的排序列表为："+orderlist);
				for(int i=0;i<orderlist.size();i++){ //这里认为dict里面的存的结构是["f1","asc","f2"]这样的，即字段是放在一个列表里的
					String recordfield=orderlist.get(i).trim();
					if((recordfield=="asc")||(recordfield=="desc")||(recordfield=="ASC")||(recordfield=="DESC")){
						continue;
					}
					if(!outputfield.contains(recordfield)){
						//如果想要排序的字段没有出现在输出字段里则错误
						System.out.println("排序字段错误，请输入出现在输出字段里的属性");
						outputfield=new ArrayList<>();
						current_list=new ArrayList<>();
						isright=false;
						return;
						//break;
					}
					for(int j=0;j<outputfield.size();j++){ //遍历每个输出属性
						if(outputfield.get(j).equals(recordfield)){ //遍历输出字段，若该输出字段和当前排序字段相同
							if((i)==orderlist.size()-1){ //如果当前排序字段是最后一个字段
								List<String> tempt=new ArrayList<>();
								tempt.add(String.valueOf(j));
								tempt.add("ASC"); //默认为升序
								order_index.add(tempt);
							}
							else if((orderlist.get(i+1).equals("asc"))||(orderlist.get(i+1).equals("ASC"))){
								List<String> tempt=new ArrayList<>();
								tempt.add(String.valueOf(j));
								tempt.add("ASC"); //以他指定的排序顺序写入order_index
								order_index.add(tempt);
							}
							else if((orderlist.get(i+1).equals("desc"))||(orderlist.get(i+1).equals("DESC"))){
								List<String> tempt=new ArrayList<>();
								tempt.add(String.valueOf(j));
								tempt.add("DESC"); //以他指定的排序顺序写入order_index
								order_index.add(tempt);
							}
							else{
								List<String> tempt=new ArrayList<>();
								tempt.add(String.valueOf(j));
								tempt.add("ASC");
							}
							
						}
					}
				}
				System.out.println("现在的排序属性下标和升降序的列表为："+order_index);
				for(int i=0;i<order_index.size();i++){
					//遍历所有的排序序列，对每一个排序序列对输出结果进行排序
					if(order_index.get(i).get(1).equals("ASC")){ //如果是升序
						int pos=Integer.valueOf(order_index.get(i).get(0)); //取出排序字段对应的下标
						Collections.sort(current_list,new Comparator<List<String>>(){
							@Override
							public int compare(List<String> l1,List<String> l2){
								return l1.get(pos).compareTo(l2.get(pos));
								/*if(Integer.valueOf(l1.get(pos))>Integer.valueOf(l2.get(pos))){
									return 1;
								}
								else if(Integer.valueOf(l1.get(pos))==Integer.valueOf(l2.get(pos))){
									return 0;
								}
								else {
									return -1;
								}*/
							}
						});
					}
					else if(order_index.get(i).get(1).equals("DESC")){ //如果是降序
						int pos=Integer.valueOf(order_index.get(i).get(0)); //取出排序字段对应的下标
						Collections.sort(current_list,new Comparator<List<String>>(){
							@Override
							public int compare(List<String> l1,List<String> l2){
								return l2.get(pos).compareTo(l1.get(pos));
							
								/*if(Integer.valueOf(l1.get(pos))<Integer.valueOf(l2.get(pos))){
									return 1;
								}
								else if(Integer.valueOf(l1.get(pos))==Integer.valueOf(l2.get(pos))){
									return 0;
								}
								else{
									return -1;
								}*/
							}
						});
					}
				}
				System.out.println("排序以后的结果为："+current_list);
				/*if(idx==0){
					return;
				}*/
			}
			idx-=1;
		}
		System.out.println("最终的outputfield为"+outputfield);
		System.out.println("最终的current_list为"+current_list);
		//return;
	}
	public void execute_logical_tree(List<String> table_name_list) throws IOException{
		if(Node.global_logical_tree!=null){
			List<String> outputfield=new ArrayList<>();
			List<List<String>> current_list=new ArrayList<>();
			Boolean isright=false;
			execute_tree(table_name_list,Node.global_logical_tree);
			if(isright){
				System.out.println("输出的字段为：");
				System.out.println(outputfield);
				System.out.println("查询结果为：");
				for(int i=0;i<current_list.size();i++){
					System.out.println(current_list.get(i));
				}
			}
			else{
				System.out.println("错误的sql语句输入！");
			}
			
		}
		else{
			System.out.println("该查询没有树");
		}
	}
	
	public void construct_logical_tree(){
		if(syn_tree!=null){
			ParseNode pn=extract_sfw_data();
			List<Object> sel_list=pn.sel_list;
			List<Object> from_list=pn.from_list;
			List<Object> where_list=pn.where_list;
			List<Object> order_list=pn.order_list;
			for(int i=0;i<sel_list.size();i++){
				if(sel_list.get(i)==","){
					sel_list.remove(i);
				}
			}
			for(int i=0;i<from_list.size();i++){
				if(from_list.get(i)==","){
					from_list.remove(i);
				}
			}
			for(int i=0;i<order_list.size();i++){
				if(order_list.get(i)==","){
					order_list.remove(i);
				}
			}
			//where_list先不处理
			System.out.println(sel_list);
			System.out.println(from_list);
			System.out.println(where_list);
			System.out.println(order_list);
			
			Node from_node=construct_from_node(from_list);
			Node where_node=construct_where_node(from_node,where_list);
			Node select_node=construct_select_node(where_node,sel_list);
			Node distinct_node=construct_distinct_select_node(select_node);
			Node.global_logical_tree=construct_order_node(distinct_node,order_list);
			if(Node.global_logical_tree!=null){
				Node.show(Node.global_logical_tree);
			}
		}
		else{
			System.out.println("没有数据在语法树里");
		}
	}
	
	
	
}
