package xmj.minibase01;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseNode2 {
	public List<Object> sel_list;
	public List<Object> from_list;
	public List<Object> where_list;
	public List<Object> order_list;
	public Node syn_tree=Node.global_syn_tree;
	public List<String> outputfield=new ArrayList<>();
	public List<List<String>> current_list=new ArrayList<>();
	public Boolean isright=true;
	
	public ParseNode2(){
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
	
	public ParseNode2 extract_sfw_data(){
		//System.out.println("--------现在开始执行提取sfw数据操作------");
		if(syn_tree==null){
			System.out.println("错误！");
			return null;
		}
		else{
			System.out.println("现在语法树不为空");
			ParseNode2 PN=new ParseNode2();
			destruct(syn_tree,PN); //解析这棵语法树，获得sellist,fromlist,wherelist,orderlist
			return PN;
		}
	}
	//tmplist里面保存的是nodeobj的value值 ，其实应该就是需要找出的每个sellist,fromlist,wherelist等里面的属性、关系名等
	/*public void show(Node nodeobj,List<Object> tmplist){
		//nodeobj.show(nodeobj);
		Node node=nodeobj;
		node.show(node);
		if(nodeobj.children==null){ //当前节点为叶节点
			System.out.println("现在进入了孩子节点");
			Object o1=nodeobj.value;
			tmplist.add(o1);
		}
		else{
			for(int i=0;i<nodeobj.children.size();i++){
				show(nodeobj.children.get(i),tmplist);
			}
		}
	}*/
	//从语法树里获得sellist,fromlist,wherelist,orderlist
	public void destruct(Node nodeobj,ParseNode2 PN){
		if(nodeobj.children!=null){
			if(nodeobj.value=="SelList"){
				nodeobj.show(nodeobj);
				List<Object> tmplist=new ArrayList<>();
				nodeobj.get_child(nodeobj,tmplist); //得到这个节点对应的子树的所有叶节点的值
				//System.out.println(tmplist);
				PN.update_sel_list(tmplist);
			}
			else if(nodeobj.value=="FromList"){
				List<Object> tmplist=new ArrayList<>();
				nodeobj.get_child(nodeobj,tmplist);
				//System.out.println(tmplist);
				PN.update_from_list(tmplist);
			}
			else if(nodeobj.value=="WhereList"){
				List<Object> tmplist=new ArrayList<>();
				nodeobj.get_child(nodeobj,tmplist);
				//System.out.println(tmplist);
				PN.update_where_list(tmplist);
			}
			else if(nodeobj.value=="OrderList"){
				List<Object> tmplist=new ArrayList<>();
				nodeobj.get_child(nodeobj,tmplist);
				//System.out.println(tmplist);
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
		if(fromlist.size()!=0){ //fromlist列表不为空
			if(fromlist.size()==1){ //只有一个表名
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
				//每次选择最后一个元素值去生成一个叶节点
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
		Node.traver(selnode, sqlsent); //sqlsent里面是selnode子树上所有节点的value值
		//System.out.println(sqlsent);
		if(!sqlsent.contains("Distinct")){
			return selnode;
		}
		else{
			List<Node> templist=new ArrayList<>();
			templist.add(selnode);
			node=new Node("Distinct",templist,null); //将selnode作为Distinct节点的孩子
		}
		return node;
	}
	
	public Node construct_order_node(Node distinct_node,List<Object> orderlist){
		Node node=new Node();
		if((distinct_node!=null)&&(orderlist.size()>0)){
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
		//System.out.println("原来的dict[where]"+dict.get(idx));
		List<Object> l1=new ArrayList<>(); 
		//System.out.println("这里的nodeobj的value为"+nodeobj.value);
		Object o1=nodeobj.value;
		l1.add(o1); //首先将以value为对象加入列表
		dict.get(idx).add(l1);
		if(nodeobj.var!=null){
			//System.out.println("这里的var不为空");
			int len=dict.get(idx).size();
			List<Object> list=new ArrayList<>();
			//System.out.println("他的var列表为"+nodeobj.var);
			Object o2=nodeobj.var; //将nodeobj.var转变成object类型
			Object o3=dict.get(idx).get(len-1); //将原来的最后一个元素也转变成object类型
			list.add(o3);
			list.add(o2);
		//	System.out.println(list);
			dict.get(idx).set(len-1, list);
			/*dict.get(idx).remove(len-1); //删除原来的最后一个元素
			dict.get(idx).add(list);*/
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
		List<List<String>> result=new ArrayList<>();
		while(input.size()>1){ //当长度大于1
			List<List<List<String>>> temp_input=new ArrayList<>();
			temp_input.add(input.get(0));
			temp_input.add(input.get(1));
			List<List<String>> temp_field=new ArrayList<>();
			temp_field.add(field.get(0));
			temp_field.add(field.get(1));
			input.remove(0);
			input.remove(0);
			result=nature_link1(temp_input,temp_field);
			input.add(0, result);
			field.remove(0);
			field.remove(0);
			List<String> temp_f=new ArrayList<>();
			for(int i=0;i<temp_field.size();i++){
				for(int j=0;j<temp_field.get(i).size();j++){
					if(!temp_f.contains(temp_field.get(i).get(j))){
						temp_f.add(temp_field.get(i).get(j));
					}
				}
			}
			field.add(0, temp_f);
		}
		return result;
		
	}
	
	public List<List<String>> nature_link1(List<List<List<String>>> input,List<List<String>> field){
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
		int idx=0;
		HashMap<Integer,List<List<Object>>> dict=new HashMap<>();
		build_execute_tree(nodeobj,idx,dict);
		
		Set<Integer> keys=dict.keySet(); //获取dict的键值set
		List<Integer> key_list=new ArrayList<Integer>(keys); //set转换成list 
		//对key_List降序排列
		Collections.reverse(key_list); //将key_list逆转
		int fromidx=key_list.get(0);		
		idx=key_list.get(0); //初始现在idx应该为最底层from
		//System.out.println("现在的idx为"+idx);
		List<List<String>> current_field=new ArrayList<>();
		List<String> tablename_order=new ArrayList<>();
		
		List<List<String>> fieldnamelist=new ArrayList<>();
		List<List<List<String>>> recordlist=new ArrayList<>();
		List<String> fromnamelist=new ArrayList<>();
		List<String> allsel_field=new ArrayList<>(); //存储出现在select和where里面的字段
		while(idx>=0){
			//System.out.println("现在的idx为："+idx);
			if(idx==fromidx){
				isright=true;
				//System.out.println("-------------------------------------------");
				//System.out.println("现在执行 的是最底层from节点");
				//System.out.println("---------"+dict.get(idx)+"---------");
				if(dict.get(idx).size()>1){  //如果fromlist长度>1
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
					Object o1=dict.get(idx).get(0).get(0);
					Object o2=dict.get(idx).get(1).get(0);
					String tablename1=o1.toString();
					String tablename2=o2.toString();
					Storage dataobj1=new Storage(tablename1); //生成两个数据对象
					Storage dataobj2=new Storage(tablename2);
					
					List<List<String>> recordlist1=dataobj1.record_list;
					List<List<String>> recordlist2=dataobj2.record_list;
					Stream<List<String>> st1=dataobj1.field_name_list.stream();
					List<String> fieldnamelist1=st1.map((x)->x.get(0)).collect(Collectors.toList());
					
					Stream<List<String>> st2=dataobj2.field_name_list.stream();
					List<String> fieldnamelist2=st2.map((x)->x.get(0)).collect(Collectors.toList());
					
					fieldnamelist.add(fieldnamelist1);
					fieldnamelist.add(fieldnamelist2);
					recordlist.add(recordlist1);  //将两张表的域名信息加入总的域名信息列表
					recordlist.add(recordlist2);
					fromnamelist.add(tablename1);
					fromnamelist.add(tablename2);
				}
				else{ //只有一张表
					Object o=dict.get(idx).get(0).get(0);
					String tablename=o.toString();
					Storage dataobj=new Storage(tablename);
					Stream<List<String>> st1=dataobj.field_name_list.stream();
					List<String> fieldnamelist1=st1.map((x)->x.get(0)).collect(Collectors.toList());
					
					List<List<String>> recordlist1=dataobj.record_list;
					fieldnamelist.add(fieldnamelist1);
					recordlist.add(recordlist1);
					fromnamelist.add(tablename);
				}
				//System.out.println("底层from里面的记录列表为"+recordlist);
				//System.out.println("底层from里面的域名列表为"+fieldnamelist);
			}
			else if((dict.get(idx).get(0).contains("From"))&&(dict.get(idx).size()>1)){
				//只有有第三张表的时候才会执行这个分支 
				//System.out.println("--------------------------------");
				//System.out.println("现在执行的是上层的from");
				isright=true;
				Object table=dict.get(idx).get(1).get(0);
				String tablename=table.toString();
				Storage dataobj=new Storage(tablename);
				Stream<List<String>> st1=dataobj.field_name_list.stream();
				List<String> fieldnamelist1=st1.map((x)->x.get(0)).collect(Collectors.toList());
				
				List<List<String>> recordlist1=dataobj.record_list;
				fieldnamelist.add(fieldnamelist1);
				recordlist.add(recordlist1);
				fromnamelist.add(tablename);
				//System.out.println("上层from里面的记录列表为"+recordlist);
				//System.out.println("上层from里面的域名列表为"+fieldnamelist);
			}
			else if((dict.get(idx).get(0).contains("From"))&&(dict.get(idx).get(0).size()==1)){
				idx-=1;
				continue;
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Where")){
				isright=true;
				//System.out.println("--------------------------------");
				//System.out.println("现在执行的是where");
				//System.out.println(((List)dict.get(idx)));
				Object condlist=dict.get(idx).get(0).get(1); //获取where的条件
				List<List<String>> filter=(List)condlist;
				//System.out.println(filter);
				for(int i=0;i<filter.size();i++){
					List<String> cond=filter.get(i); //获取第i个条件
					//System.out.println("第"+i+"个条件"+cond);
					if("=".equals(cond.get(1))){ //进行的是等值判断
						String ch1=cond.get(0);
						String ch2=cond.get(2);
						if((ch1.contains("."))&&(ch2.contains("."))){
							//如果进行的是等值连接 ,则跳过该条件，因为自然连接就是满足等值连接的
							allsel_field.add(ch1); //把等值连接的字段加入allsel_field
							allsel_field.add(ch2);
							continue;
						}
						else{ //是简单的等值判断，等号左边是字段名，右边是常量
							//System.out.println("现在进行的是等值判断条件"+cond);
							List<List<Integer>> ind=new ArrayList<>();
							for(int j=0;j<fieldnamelist.size();j++){ //遍历每个表的域名列表
								for(int k=0;k<fieldnamelist.get(j).size();k++){
									if(ch1.equals(fieldnamelist.get(j).get(k).trim())){
										List<Integer> temp_ind=new ArrayList<>();
										temp_ind.add(j);
										temp_ind.add(k); //记下条件左边字段所在的位置（哪个表的那个下标的字段上）
										ind.add(temp_ind);
										break;
									}
								}
								/*if(ind.size()!=0){ //表示已经找到了这个字段对应的下标，其余的表的域名信息则不需要再看
									break;
								}*/
							}
							//System.out.println(ind);
							//List<List<List<String>>> temp_record=new ArrayList<>();
							if(ind.size()!=0){ //且大小只会为1，因为等值判断的字段一定是一个表的字段，不会存在在多张表里（这样就会是等值连接了），
								//System.out.println(ch1+"字段对应的下标"+ind);
								for(int t=0;t<ind.size();t++){
									
									List<List<String>> temp_recordlist=recordlist.get(ind.get(t).get(0)); //获取该字段对应的表的记录
									List<List<String>> new_recordlist=new ArrayList<>(); //存储该字段对应的表的满足等值判断条件的记录
									for(int j=0;j<temp_recordlist.size();j++){
										List<String> re=temp_recordlist.get(j); //遍历取每条记录
										if(re.get(ind.get(t).get(1)).trim().equals(ch2)){ //如果这条记录的该字段上的取值和条件里的常量相等，则表示该记录值满足等值判断条件的记录
											new_recordlist.add(re);
										}
									}
									//temp_record.add(new_recordlist); 
									recordlist.set(ind.get(t).get(0), new_recordlist);
									/*recordlist.remove(ind.get(t).get(0)); //删除原来该表的记录列表
									recordlist.add(ind.get(t).get(0), new_recordlist); //存入新的满足等值判断条件的该表的记录列表
*/									//allsel_field.add(ch1); //将这个等值判断条件的字段加入列表，为了之后投影做准备
								}
								//System.out.println("现在输出的是经过等值判断条件判断的满足条件的记录");
								//System.out.println(recordlist);
							}
							else{
								System.out.println("等值判断的字段不存在！错误！");
								outputfield=new ArrayList<>();
								current_list=new ArrayList<>();
								isright=false;
								return;
							}
						}
					}
					else if("LIKE".equals(cond.get(1))||"like".equals(cond.get(1))){
						//如果进行的是相似性查询
						String ch1=cond.get(0);
						String ch2=cond.get(2);
						//System.out.println("现在执行的是相似性查询");
						List<List<Integer>> ind=new ArrayList<>();
						for(int j=0;j<fieldnamelist.size();j++){
							List<Integer> temp=new ArrayList<>();
							for(int k=0;k<fieldnamelist.get(j).size();k++){
								if(ch1.equals(fieldnamelist.get(j).get(k).trim())){//如果该条件的属性是该表的属性，则存下该属性的出现位置
									temp.add(j);
									temp.add(k);
									ind.add(temp);
								}
							}
						}
						if(ind.size()!=0){ //表示存在这个属性
							for(int j=0;j<ind.size();j++){
								//找到该表的记录列表
								List<List<String>> new_recordlist=new ArrayList<>();
								List<List<String>> temp_recordlist=recordlist.get(ind.get(j).get(0));
								for(int k=0;k<temp_recordlist.size();k++){ //遍历该表的每条记录
									String fie=temp_recordlist.get(k).get(ind.get(j).get(1)).trim(); //获取该记录在该属性上的取值
									if(ch2.startsWith("%")){ //如果%在开头，即匹配以...结尾的元组
										String subch2=ch2.substring(1);
										int ch2len=subch2.length();
										if(fie.substring(fie.length()-ch2len).equals(subch2)){  //如果满足该记录的该属性的末尾确实是给定的常量
											new_recordlist.add(temp_recordlist.get(k)); 
										}
									}
									else if(ch2.endsWith("%")){ //如果%在末尾，表示匹配以...开头的元组
										String subch2=ch2.substring(0,ch2.length()-1); //取前面的匹配部分
										int ch2len=subch2.length();
										if(fie.substring(0,ch2len).equals(subch2)){ //如果满足该记录的该属性的前面确实是指定的常量
											new_recordlist.add(temp_recordlist.get(k));
										}
									}
									else if((ch2.indexOf("%")!=0)||(ch2.indexOf("%")!=ch2.length()-1)){
										//如果%的索引不在开头和结尾
										//System.out.println("现在匹配的%在中间");
										String subch21=ch2.substring(0, ch2.indexOf("%"));
										int len1=subch21.length();
										String subch22=ch2.substring(ch2.indexOf("%")+1);
										int len2=subch22.length();
										if((fie.substring(0,len1).equals(subch21))&&(fie.substring(fie.length()-len2).equals(subch22))){
											new_recordlist.add(temp_recordlist.get(k));
										}
									}
										
									
								}
								recordlist.set(ind.get(j).get(0), new_recordlist); //替换掉原来的记录列表
							}
						}
						else{
							System.out.println("查询的字段不存在！错误！");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
					}
					else if("<".equals(cond.get(1))){
						String ch1=cond.get(0);
						String ch2=cond.get(2);
						//System.out.println("现在进行的是小于判断条件"+cond);
						List<List<Integer>> ind=new ArrayList<>();
						for(int j=0;j<fieldnamelist.size();j++){ //遍历每个表的域名列表
							for(int k=0;k<fieldnamelist.get(j).size();k++){
								if(ch1.equals(fieldnamelist.get(j).get(k).trim())){
									List<Integer> temp_ind=new ArrayList<>();
									temp_ind.add(j);
									temp_ind.add(k); //记下条件左边字段所在的位置（哪个表的那个下标的字段上）
									ind.add(temp_ind);
									break;
								}
							}
						}
						//System.out.println(ind);
						if(ind.size()!=0){ //且大小只会为1，因为等值判断的字段一定是一个表的字段，不会存在在多张表里（这样就会是等值连接了），
							//System.out.println(ch1+"字段对应的下标"+ind);
							for(int t=0;t<ind.size();t++){
								
								List<List<String>> temp_recordlist=recordlist.get(ind.get(t).get(0)); //获取该字段对应的表的记录
								List<List<String>> new_recordlist=new ArrayList<>(); //存储该字段对应的表的满足等值判断条件的记录
								for(int j=0;j<temp_recordlist.size();j++){
									List<String> re=temp_recordlist.get(j); //遍历取每条记录
									if(re.get(ind.get(t).get(1)).trim().compareTo(ch2)<0){ //如果这条记录的该字段上的取值小于条件里的常量，则表示该记录值满足等值判断条件的记录
										new_recordlist.add(re);
									}
								}
								recordlist.set(ind.get(t).get(0), new_recordlist);
							}
							//System.out.println("现在输出的是经过小于判断条件判断的满足条件的记录");
							//System.out.println(recordlist);
						}
						else{
							System.out.println("不存在该字段，错误！");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
					}
					else if(">".equals(cond.get(1))){
						String ch1=cond.get(0);
						String ch2=cond.get(2);
						//System.out.println("现在进行的是大于判断条件"+cond);
						List<List<Integer>> ind=new ArrayList<>();
						for(int j=0;j<fieldnamelist.size();j++){ //遍历每个表的域名列表
							for(int k=0;k<fieldnamelist.get(j).size();k++){
								if(ch1.equals(fieldnamelist.get(j).get(k).trim())){
									List<Integer> temp_ind=new ArrayList<>();
									temp_ind.add(j);
									temp_ind.add(k); //记下条件左边字段所在的位置（哪个表的那个下标的字段上）
									ind.add(temp_ind);
									break;
								}
							}
						}
						//System.out.println(ind);
						if(ind.size()!=0){ //且大小只会为1，因为等值判断的字段一定是一个表的字段，不会存在在多张表里（这样就会是等值连接了），
							//System.out.println(ch1+"字段对应的下标"+ind);
							for(int t=0;t<ind.size();t++){
								
								List<List<String>> temp_recordlist=recordlist.get(ind.get(t).get(0)); //获取该字段对应的表的记录
								List<List<String>> new_recordlist=new ArrayList<>(); //存储该字段对应的表的满足等值判断条件的记录
								for(int j=0;j<temp_recordlist.size();j++){
									List<String> re=temp_recordlist.get(j); //遍历取每条记录
									if(re.get(ind.get(t).get(1)).trim().compareTo(ch2)>0){ //如果这条记录的该字段上的取值小于条件里的常量，则表示该记录值满足等值判断条件的记录
										new_recordlist.add(re);
									}
								}
								recordlist.set(ind.get(t).get(0), new_recordlist);
							}
							//System.out.println("现在输出的是经过小于判断条件判断的满足条件的记录");
							//System.out.println(recordlist);
						}
						else{
							System.out.println("不存在该字段，错误！");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
					}
				}
				//System.out.println("经过where筛选记录列表为"+recordlist);
				//System.out.println("经过where筛选的域名列表为"+fieldnamelist);
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Sel")){
				isright=true;
				//System.out.println("--------------------------------");
				//System.out.println("现在执行的是select");
				Object obj=dict.get(idx).get(0).get(1);
				List<String> select=(List)obj; //获取sellist
				//System.out.println("现在的select列表为"+select);
				if(select.get(0).contains("*")){ //如果包含*号的话，表示from列表只有一个表，将该表的所有字段均加入
					for(int j=0;j<fieldnamelist.get(0).size();j++){
						allsel_field.add(fieldnamelist.get(0).get(j));
					}
				}
				else{
					for(int i=0;i<select.size();i++){
						allsel_field.add(select.get(i)); //将sellist里面的所有的字段都加到allsel_field里面
					}
				}
				
				HashMap<Integer,List<Integer>> map=new HashMap<>(); //存储每个表，以及该表要保留的字段的下标
				for(int i=0;i<fromnamelist.size();i++){ //遍历每张表
					List<Integer> temp=new ArrayList<>(); //存储这张表要保留的字段的下标
					for(int j=0;j<allsel_field.size();j++){ //遍历所有要保留的字段
						if(!allsel_field.get(j).contains(".")){ //处理的不是相同字段
							for(int k=0;k<fieldnamelist.get(i).size();k++){ //遍历这张表的字段列表
								if(allsel_field.get(j).equals(fieldnamelist.get(i).get(k).trim())){
									temp.add(k);
									//allsel_field.remove(j);
								}
							}
						
						}
						else{ //是相同字段
							String[] str=allsel_field.get(j).split("\\.");
							String table1=str[0];
							String field1=str[1];
							if(table1.equals(fromnamelist.get(i).trim())){//是当前表的字段
								for(int k=0;k<fieldnamelist.get(i).size();k++){
									if(field1.equals(fieldnamelist.get(i).get(k).trim())){//找到该字段的下标
										temp.add(k);
									}
								}
							}
						}
					}
					map.put(i, temp);
				}
				//System.out.println(map);
				Set key1=map.keySet();
				List<Integer> keylist=new ArrayList<>(key1);
				for(int i=0;i<keylist.size();i++){ //遍历每张表
					List<List<String>> new_record=new ArrayList<>(); //保存该表的新的记录（经投影以后的记录）
					List<Integer> li=map.get(i);  //当前该表被投影的字段的下标列表
					
					for(int k=0;k<recordlist.get(i).size();k++){ //遍历该表的每条记录
						List<String>  temp=new ArrayList<>();
						for(int j=0;j<li.size();j++){ //取该条记录的这些字段上的值
							temp.add(recordlist.get(i).get(k).get(li.get(j)));
						}
						new_record.add(temp);
					}
					recordlist.remove(i);
					recordlist.add(i,new_record); //存入新的记录列表
					
					List<String> new_field=new ArrayList<>(); //生成新的 域名列表（只包含每个表被投影出来的字段）
					for(int k=0;k<li.size();k++){
						new_field.add(fieldnamelist.get(i).get(li.get(k)));
					}
					fieldnamelist.remove(i);
					fieldnamelist.add(i,new_field); //存入新的域名列表
				}
				//System.out.println("下面输出的是经过投影以后的记录列表");
				//System.out.println(recordlist);
				//System.out.println(fieldnamelist);
				//System.out.println("----------------------------");
				List<List<String>> result=new ArrayList<>();
				if(recordlist.size()>1){
					result=nature_link(recordlist,fieldnamelist);
					//System.out.println("自然连接的结果为："+result);
					//System.out.println("+++++++++++++++++++++++++++++");
					for(int i=0;i<fieldnamelist.size();i++){
						for(int j=0;j<fieldnamelist.get(i).size();j++){
							if(!outputfield.contains(fieldnamelist.get(i).get(j))){
								outputfield.add(fieldnamelist.get(i).get(j)); //里面存的是非重复的属性
							}
						}
					}
				}
				else{
					result=recordlist.get(0);
					//System.out.println("这个结果为"+result);
					outputfield=fieldnamelist.get(0);
				}
				//System.out.println("下面输出的是outputfield"+outputfield);
				//System.out.println("--------------------------");
				//下面从获得自然连接的结果中，投影出select真正选出的字段对应的信息
				List<List<String>> temp_recordlist=result;
				List<Integer>  temp=new ArrayList<>(); //记录select出的字段在自然连接以后的记录列表的下标
				result=new ArrayList<>();
				for(int i=0;i<select.size();i++){
					for(int j=0;j<outputfield.size();j++){
						if(select.get(i).trim().equals(outputfield.get(j).trim())){
							temp.add(j); //记下选出的字段在输出域的下标
						}
					}
				}
				for(int i=0;i<temp_recordlist.size();i++){
					List<String> temp_re=new ArrayList<>();
					for(int j=0;j<temp.size();j++){
						temp_re.add(temp_recordlist.get(i).get(temp.get(j)));
					}
					result.add(temp_re); //这就是最后的结果，current_list
				}
				current_list=result;
				outputfield=select;
				//System.out.println("下面输出select最后的结果：");
				//System.out.println(current_list);
				//System.out.println(outputfield);
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Distinct")){
				//System.out.println("--------------------------------");
				//System.out.println("现在执行的是distinct");
				isright=true;
				List<List<String>> temp_current_list=new ArrayList<>();
				temp_current_list=current_list;
				current_list=new ArrayList<>();
				for(int i=0;i<temp_current_list.size();i++){
					if(!current_list.contains(temp_current_list.get(i))){ //没有出现在temp_current_list里面的记录才加入现在的current_list
						current_list.add(temp_current_list.get(i));
					}
				}
				//System.out.println("下面输出distinct最后的结果：");
				//System.out.println(current_list);
				//System.out.println(outputfield);
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Order")){
				//System.out.println("--------------------------------");
				//System.out.println("现在执行的是order");
				isright=true;
				List<List<String>> order_index=new ArrayList<>();
				List<String> orderlist=new ArrayList<>();
				orderlist=(List)dict.get(idx).get(0).get(1);
				//System.out.println("现在的排序列表为："+orderlist);
				for(int i=0;i<orderlist.size();i++){ //这里认为dict里面的存的结构是["f1","asc","f2"]这样的，即字段是放在一个列表里的
					String recordfield=orderlist.get(i).trim();
					if((recordfield=="asc")||(recordfield=="desc")||(recordfield=="ASC")||(recordfield=="DESC")){
						continue;
					}
					if(!outputfield.contains(recordfield)){
						//如果想要排序的字段没有出现在输出字段里则错误
						//System.out.println("排序字段错误，请输入出现在输出字段里的属性");
						outputfield=new ArrayList<>();
						current_list=new ArrayList<>();
						isright=false;
						return;
					}
					for(int j=0;j<outputfield.size();j++){ //遍历每个输出属性
						if(outputfield.get(j).trim().equals(recordfield.trim())){ //遍历输出字段，若该输出字段和当前排序字段相同
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
				//System.out.println("现在的排序属性下标和升降序的列表为："+order_index);
				for(int i=0;i<order_index.size();i++){
					//遍历所有的排序序列，对每一个排序序列对输出结果进行排序
					if(order_index.get(i).get(1).equals("ASC")){ //如果是升序
						int pos=Integer.valueOf(order_index.get(i).get(0)); //取出排序字段对应的下标
						Collections.sort(current_list,new Comparator<List<String>>(){
							@Override
							public int compare(List<String> l1,List<String> l2){
								return l1.get(pos).compareTo(l2.get(pos));
							}
						});
					}
					else if(order_index.get(i).get(1).equals("DESC")){ //如果是降序
						int pos=Integer.valueOf(order_index.get(i).get(0)); //取出排序字段对应的下标
						Collections.sort(current_list,new Comparator<List<String>>(){
							@Override
							public int compare(List<String> l1,List<String> l2){
								return l2.get(pos).compareTo(l1.get(pos));
							}
						});
					}
				}
				//System.out.println("排序以后的结果为："+current_list);
			}
			idx-=1;
		}
		System.out.println("最终的结果列表为："+current_list);
		System.out.println("最终的字段为："+outputfield);
		isright=true;
	}
	
	public void execute_logical_tree(List<String> table_name_list) throws IOException{
		if(Node.global_logical_tree!=null){
			//System.out.println("逻辑树已经构造好");
			Node.global_logical_tree.show(Node.global_logical_tree);
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
	
	public void construct_logical_tree() throws IOException{
		if(syn_tree!=null){
			System.out.println("下面输出的是语法树");
			syn_tree.show(syn_tree);
			System.out.println("-------------------------");
			ParseNode2 pn=extract_sfw_data();
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
			int pos=-1;
			for(int i=0;i<where_list.size();i++){
				if(where_list.get(i).equals("AND")){
					pos=i;
					break;
				}
			}
			if(pos!=-1){
				List<Object> wherelist1=new ArrayList<>();
				for(int j=0;j<pos;j++){ //将and之前的放在一个列表里去成为一个条件
					wherelist1.add(where_list.get(j));
				}
				
				List<Object> wherelist2=new ArrayList<>();
				for(int j=pos+1;j<where_list.size();j++){
					wherelist2.add(where_list.get(j));
				}
				where_list=new ArrayList<>();
				where_list.add(wherelist1);
				where_list.add(wherelist2);
				
			}
			else{
				List<Object> wherelist1=new ArrayList<>();
				for(int j=0;j<where_list.size();j++){
					wherelist1.add(where_list.get(j));
				}
				where_list=new ArrayList<>();
				where_list.add(wherelist1);
			}
			
			//System.out.println("这是sellist"+sel_list);
			//System.out.println("这是fromlist"+from_list);
			//System.out.println("这是wherelist"+where_list);
			//System.out.println("这是orderlist"+order_list);
			
			Node from_node=construct_from_node(from_list);
			
			Node where_node=construct_where_node(from_node,where_list);
			Node select_node=construct_select_node(where_node,sel_list);
			Node distinct_node=construct_distinct_select_node(select_node);
			Node.global_logical_tree=construct_order_node(distinct_node,order_list);
			if(Node.global_logical_tree!=null){
				System.out.println("下面输出的是逻辑树");
				Node.show(Node.global_logical_tree);
				System.out.println("----------------");
			}
			Schema obj=new Schema(); //生成一个模式对象
			List<String> table_name_list=obj.get_tablenamelist(); //获取表名列表
			execute_logical_tree(table_name_list); //执行查询，获得查询结果
		}
		else{
			System.out.println("没有数据在语法树里");
		}
	}
	
}
