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
		System.out.println("���ڿ�ʼִ����ȡsfw���ݲ�����");
		
		Node syn_tree=Node.global_syn_tree;
		if(syn_tree==null){
			System.out.println("����");
			return null;
		}
		else{
			ParseNode PN=new ParseNode();
			destruct(syn_tree,PN);
			return PN;
		}
	}
	//tmplist���汣�����nodeobj��valueֵ ����ʵӦ�þ�����Ҫ�ҳ���ÿ��sellist,fromlist,wherelist����������ԡ���ϵ����
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
	//���﷨������sellist,fromlist,wherelist,orderlist
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
				Node tempnode=new Node((String)fromlist.get(0),null,null); //�Ա�������һ��Ҷ�ӽڵ�
				List<Node> templist=new ArrayList<>();
				templist.add(tempnode);
				node=new Node("From",templist,null); //ʹ�ø����ɵ�Ҷ�ڵ���Ϊ���ӽڵ����ɸ��ڵ�
				//return node;
			}
			else if(fromlist.size()==2){
				Node tempnode1=new Node((String)fromlist.get(0),null,null); //ʹ�����������ֱ��������Ҷ�ڵ�
				Node tempnode2=new Node((String)fromlist.get(1),null,null);
				List<Node> templist=new ArrayList<>();
				templist.add(tempnode1);
				templist.add(tempnode2);
				node=new Node("From",templist,null); //���ɸ��ڵ�
				//return node;
			}
			else if(fromlist.size()>2){
				Node tempnode=new Node((String)fromlist.get(fromlist.size()-1),null,null);
				//��ÿ��ѡ�����һ��Ԫ��ֵȥ����һ��Ҷ�ڵ�
				List<Object> temp=new ArrayList<>();
				for(int i=0;i<fromlist.size()-1;i++){ //ȥ�����һ��Ԫ�ص�from�б�
					temp.add(fromlist.get(i));
				}
				List<Node> templist=new ArrayList<>();
				templist.add(construct_from_node(temp)); //�ݹ���ù���from�ڵ㺯��
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
			templist.add(fromnode); //ʹ��from�ڵ���Ϊwhere�ڵ�ĺ���
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
			node=new Node("Sel",templist,sel_list); //ʹ��where�ڵ���Ϊsel�ڵ�ĺ���
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
		System.out.println("orderlistd�ĳ���Ϊ��"+orderlist.size());
		if((distinct_node!=null)&&(orderlist.size()>0)){
			System.out.println("���ڿ�ʼִ�й���order�ڵ�");
			List<Node> templist=new ArrayList<>();
			templist.add(distinct_node);
			node=new Node("Order",templist,orderlist);
		}
		else{
			return distinct_node;
		}
		return node;
	}
	
	//���ڹ�������dict,��ͬidx�ڵ��dict[idx]���Ͳ�ͬ
	public void build_execute_tree(Node nodeobj,int idx,HashMap<Integer,List<List<Object>>> dict){
		if(!dict.containsKey(idx)){ //��û�������ֵ����ʱ��Ҫ��ʼ��һ�����б�
			dict.put(idx, new ArrayList<>()); //��ʼ��Ϊ���б�
		}
		List<Object> l1=new ArrayList<>(); 
		System.out.println("�����nodeobj��valueΪ"+nodeobj.value);
		Object o1=nodeobj.value;
		l1.add(o1); //���Ƚ���valueΪ��������б�
		/*List<List<Object>> list1=dict.get(idx);
		list1.add(l1);
		dict.put(idx,list1);*/
		dict.get(idx).add(l1);
		if(nodeobj.var!=null){
			System.out.println("�����var��Ϊ��");
			int len=dict.get(idx).size();
			List<Object> list=new ArrayList<>();
			System.out.println("����var�б�Ϊ"+nodeobj.var);
			Object o2=nodeobj.var; //��nodeobj.varת���object����
			Object o3=dict.get(idx).get(len-1); //��ԭ�������һ��Ԫ��Ҳת���object����
			list.add(o3);
			list.add(o2);
			
			dict.get(idx).remove(len-1); //ɾ��ԭ�������һ��Ԫ��
			dict.get(idx).add(list);
		}
		if(nodeobj.children!=null){
			for(int i=0;i<nodeobj.children.size();i++){
				build_execute_tree(nodeobj.children.get(i),idx+1,dict);
			}
		}
	}
	
	public List<List<String>> dikaerji(List<List<List<String>>> input){
		List<List<String>> a0=input.get(0); //��ȡ�ѿ������ĵ�һ������
		List<List<String>> temp=new ArrayList<>();
		for(int i=1;i<input.size();i++){
			List<List<String>> a1=input.get(i);  //��ȡ�ѿ������ļ������������Ӽ���
			
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
		List<String> f0=field.get(0); //ȡ��һ����������б�
		List<List<Integer>> temp_index=new ArrayList<>();
		for(int i=1;i<field.size();i++){
			List<String> f1=field.get(i); //��ȡ�����ĵ�i����������б�
			for(int j=0;j<f0.size();j++){
				for(int k=0;k<f1.size();k++){
					if(f0.get(j).equals(f1.get(k))){ //������������������ͬ
						List<Integer> temp=new ArrayList<>();
						temp.add(j);
						temp.add(k);
						temp_index.add(temp); //����洢����  [0.j��i.k]����ʾ��0����ĵ�j�����Ժ͵�i����ĵ�k��������ͬ
					}
				}
			}
		}
		List<List<String>> result=new ArrayList<>(); //������
		if(temp_index.size()!=0){ //���������ͬ����
			List<List<String>> a0=input.get(0); //��ȡ��һ����ļ�¼�б�
			for(int i=1;i<input.size();i++){
				List<List<String>> a1=input.get(i);
				for(int j=0;j<a0.size();j++){ //�Ե�һ�ű��ÿ����¼
					for(int k=0;k<a1.size();k++){ //�Ե�i�ű��ÿ����¼
						List<String> re0=a0.get(j);
						List<String> re1=a1.get(k);
						Boolean flag=true;
						for(int t=0;t<temp_index.size();t++){
							if(!re0.get(temp_index.get(t).get(0)).equals(re1.get(temp_index.get(t).get(1)))){
								//��������¼����Ӧ��ͬ�����±괦��ֵ�Ƿ���ͬ
								flag=false;
								break;
							}
						}
						if(flag==true){ //��ʾ��������¼�����е���ͬ�ֶ��ϵ�����ȡֵ�����
							List<String> subre=re0; //������¼��Ȼ���ӵĽ��
							for(int m=0;m<re1.size();m++){
								for(int t=0;t<temp_index.size();t++){
									if(temp_index.get(t).get(1)!=m){
										subre.add(re1.get(m)); //subre�������re1�ķ��ظ��ֶ�
									}
								}
							}
							result.add(subre);
						}
					}
				}
			}
		}
		else{ //û����ͬ������Ȼ����ʧ��
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
		//��key_List��������
		Collections.reverse(key_list); //��key_list��ת
		//key_list.sort((o1,o2)->(o2.compareTo(o1)));
		int fromidx=key_list.get(0);		
		idx=key_list.get(0); //��ʼ����idxӦ��Ϊ��ײ�from
		//System.out.println("���ڵ�idxΪ"+idx);
		List<List<String>> current_field=new ArrayList<>();
		List<String> tablename_order=new ArrayList<>();
		while(idx>=0){
			System.out.println("���ڵ�idxΪ��"+idx);
			if(idx==fromidx){//��ǰ��fromlist
				isright=true;
				System.out.println("����ִ�� ����from�ڵ�");
				System.out.println("---------"+dict.get(idx)+"---------");
				if(dict.get(idx).size()>1){ //���fromlist����>1
					for(int i=0;i<dict.get(idx).size();i++){ //��֤from_List�����ÿ�����Ƿ��ǺϷ��ģ���������Ĵ������ݿ�ı�
						Object table=dict.get(idx).get(i).get(0);
						String tablename=table.toString();
						if(!table_name_list.contains(tablename)){
							System.out.println("������"+table+"�ڱ��б���");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
					}
					//��������������������ݶ���
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
						current_field.add(dataobj1.field_name_list.get(j)); //����1��������Ϣ����current_field
						outputfield.add(table1+"."+dataobj1.field_name_list.get(j).get(0).trim()); //Ԫ��Ϊ ����.����
					}
					for(int j=0;j<dataobj2.field_name_list.size();j++){
						current_field.add(dataobj2.field_name_list.get(j));
						outputfield.add(table2+"."+dataobj2.field_name_list.get(j).get(0).trim()); //Ԫ��Ϊ ����.����
					}
					List<List<String>> record1=dataobj1.getrecord();
					List<List<String>> record2=dataobj2.getrecord();
					List<List<List<String>>> list=new ArrayList<>();
					list.add(record1);
					list.add(record2);
					current_list=dikaerji(list); //��ȡ�ѿ������Ľ��
					
				}
				else{ //from_list����ֻ��һ����������ֻ��һ����
					
					Object table=dict.get(idx).get(0).get(0); //��ȡΨһ�ı���
					String tablename=table.toString();
					Storage dataobj=new Storage(tablename); //�½�һ�����ݶ���
					current_list=dataobj.getrecord();
					System.out.println("���ڵ�current_listΪ"+current_list);
					for(int i=0;i<dataobj.field_name_list.size();i++){
						outputfield.add(dataobj.field_name_list.get(i).get(0).trim());
						
					}
					tablename_order=new ArrayList<>();
					tablename_order.add(tablename);
					current_field=dataobj.field_name_list;
				}
				
			}
			else if((dict.get(idx).get(0).contains("From"))&&(dict.get(idx).size()>1)){
				//ֻ�е��е�����fromlistʱ�Ż�ִ�������֧   [[from,[...]]]      [from,[...]]              [....]
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
				templist=current_list; //������ԭ���Ľ��
				current_list=new ArrayList<>(); //���·��䣬�ÿ�
				List<List<List<String>>> temp=new ArrayList<>();
				temp.add(templist);
				temp.add(dataobj.getrecord());
				current_list=dikaerji(temp); //����ǰ��ĵѿ�����������µĵ�������ĵѿ������Ľ��
			}
			else if((dict.get(idx).get(0).contains("From"))&&(dict.get(idx).get(0).size()==1)){
				idx-=1;
				continue;
			}
			//else if(idx==1){
			else if(((List)dict.get(idx).get(0).get(0)).contains("Where")){
				System.out.println("���ڿ�ʼִ��where����");
				//System.out.println("------"+dict.get(idx)+"---------");
				isright=true;
				System.out.println("------"+dict.get(idx)+"--"+dict.get(idx).get(0)+"--"+dict.get(idx).get(0).get(1)+"----------");
				
				List<List<String>> filter=new ArrayList<>();
				filter=(List)dict.get(idx).get(0).get(1); //ת����list
				System.out.println("���ڵ�����Ϊ��"+filter);
				for(int i=0;i<filter.size();i++){ //����ÿ������
					List<String> choice=filter.get(i);
					if("="==choice.get(1)){ //���е��ǵ�ֵ�ж�
						String ch1=choice.get(0);
						String ch2=choice.get(2);
						if((ch1.contains("."))&&(ch2.contains("."))){ //�������������ֶ���.����ʾ����ֶ�����������ظ��ֶΣ���������������ֶΣ������ǵ�ֵ����
							//��ô��Ҫ��һ����¼����ɾ���ظ��ֶ�ֵ
							System.out.println("�����ǵ�ֵ�����ж�");
							if((!outputfield.contains(ch1))||(!outputfield.contains(ch2))){
								//�������������ֶ����Բ��ڱ�������б����ʾ��������
								outputfield=new ArrayList<>();
								current_list=new ArrayList<>();
								isright=false;
								return;
								
							}
							int con1=outputfield.indexOf(ch1); //�ҵ���һ���������±�
							int con2=-1;
							for(int j=outputfield.size()-1;j>=0;j--){
								if(outputfield.get(j).trim().equals(ch2)){
									con2=j;
									break;
								}
							}
							List<List<String>> temp_current_list=new ArrayList<>();
							temp_current_list=current_list; //������ԭ���ĵѿ�����
							System.out.println("ԭ���ĵѿ�����Ϊ"+temp_current_list);
							current_list=new ArrayList<>(); 
							for(int j=0;j<temp_current_list.size();j++){
								if(temp_current_list.get(j).get(con1).trim().equals(temp_current_list.get(j).get(con2).trim())){//�����ֵ����
									List<String> temp_list=new ArrayList<>();
									for(int k=0;k<temp_current_list.get(j).size();k++){
										if(k!=con2){
											temp_list.add(temp_current_list.get(j).get(k));
										}
									}
									current_list.add(temp_list); //�������ֵ������������ɾ�����ظ��ֶεļ�¼����current_list
									
								}
								
							}
							System.out.println("���ڵĽ��Ϊ"+current_list);
							List<String> temp_outputfield=new ArrayList<>();
							
							temp_outputfield=outputfield; //������ԭ��������ֶΣ���ΪҪ������ֶ�����ظ��ֶ�Ҳɾ��
							System.out.println("ԭ���������Ϊ"+temp_outputfield);
							outputfield=new ArrayList<>();
							for(int j=0;j<temp_outputfield.size();j++){ //������ֶ����еı������ֺ��ظ��������ֶ���ɾ��
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
								if(j!=con2){ //�����ǵڶ����ظ��ֶε�������Ϣ����current_field
									current_field.add(temp_current_field.get(j));
								}
							}
							System.out.println("���������������ֶ�");
							System.out.println(outputfield);
							System.out.println("����������Ǿ���where����ɾ���Ժ�ĵѿ��������");
							System.out.println(current_list);
							
						}
						else{// �ⲻ�ǵ�ֵ��������������ͨ�ĵ�ֵ�б��������Ⱥ���ߵ����ֶΣ��Ⱥ��ұߵ��ǳ���
							System.out.println("�����ǵ�ֵ�б�����");
							System.out.println(current_field);
							int con1=-1;
							for(int j=0;j<current_field.size();j++){
								if(current_field.get(j).get(0).trim().equals(ch1)){ //�ҵ�ch1��Ӧ���±�
									con1=j;
									break;
								}
							}
							if(con1==-1){ //û���ҵ������������
								System.out.println("û���ҵ��õ�ֵ������Ӧ���ֶ�");
								outputfield=new ArrayList<>();
								current_list=new ArrayList<>();
								isright=false;
								return;
							}
							int con_type=Integer.valueOf(current_field.get(con1).get(1)); //��ȡ���ֶε�����
							/*if(con_type==1){ //��int����
								int newch_1=Integer.valueOf(ch2.trim());
							}
							else if(con_type==2){ //��boolean����
								
							}*/
							List<List<String>> temp_current_list=new ArrayList<>();
							temp_current_list=current_list;
							current_list=new ArrayList<>();
							ch2=ch2.trim(); 
							for(int j=0;j<temp_current_list.size();j++){
								List<String> temprecord=new ArrayList<>();
								temprecord=temp_current_list.get(j); //��ȡ��ǰҪ�жϵļ�¼
								String ans=temprecord.get(con1).trim(); //��ȡ��ǰ��¼�����ch1�ֶε�ֵ
								if(ans.equals(ch2)){
									current_list.add(temprecord); //��ǰ��¼�ĸ��ֶ�ֵ�͵�ʽ�ұߵĳ����ȣ������current_list
								}
							}
							
						}
					}
					
					
				}
				//�����е��������д�������Ժ������������д�����ԭ���ġ�����.�ֶ������ĳɡ��ֶ�����
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
				System.out.println("���ڿ�ʼִ��select");
				System.out.println("------"+dict.get(idx)+"---------");
				isright=true;
				//System.out.println("------"+dict.get(idx)+"--"+dict.get(idx).get(0)+"--"+dict.get(idx).get(0).get(1)+"----------");
				List<String> selli=new ArrayList<>();
				selli=(List)dict.get(idx).get(0).get(1);
				if(selli.get(0)!="*"){ //����ѡ��ȫ���ֶ�
					List<Integer> selindex=new ArrayList<>();
					for(int i=0;i<selli.size();i++){
						if(!outputfield.contains(selli.get(i))){
							System.out.println("ѡ����ֶβ����ڣ�����");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
						selindex.add(outputfield.indexOf(selli.get(i))); //����ѡ���ֶ����������±�
						
					}
					outputfield=selli; //�������ֵΪselect�������ֶ��б�
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
					System.out.println("���ڵ�current_list�ǣ�"+current_list);
					
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
					if(!current_list.contains(temp_current_list.get(i))){ //û�г�����temp_current_list����ļ�¼�ż������ڵ�current_list
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
				System.out.println("���ڵ������б�Ϊ��"+orderlist);
				for(int i=0;i<orderlist.size();i++){ //������Ϊdict����Ĵ�Ľṹ��["f1","asc","f2"]�����ģ����ֶ��Ƿ���һ���б����
					String recordfield=orderlist.get(i).trim();
					if((recordfield=="asc")||(recordfield=="desc")||(recordfield=="ASC")||(recordfield=="DESC")){
						continue;
					}
					if(!outputfield.contains(recordfield)){
						//�����Ҫ������ֶ�û�г���������ֶ��������
						System.out.println("�����ֶδ������������������ֶ��������");
						outputfield=new ArrayList<>();
						current_list=new ArrayList<>();
						isright=false;
						return;
						//break;
					}
					for(int j=0;j<outputfield.size();j++){ //����ÿ���������
						if(outputfield.get(j).equals(recordfield)){ //��������ֶΣ���������ֶκ͵�ǰ�����ֶ���ͬ
							if((i)==orderlist.size()-1){ //�����ǰ�����ֶ������һ���ֶ�
								List<String> tempt=new ArrayList<>();
								tempt.add(String.valueOf(j));
								tempt.add("ASC"); //Ĭ��Ϊ����
								order_index.add(tempt);
							}
							else if((orderlist.get(i+1).equals("asc"))||(orderlist.get(i+1).equals("ASC"))){
								List<String> tempt=new ArrayList<>();
								tempt.add(String.valueOf(j));
								tempt.add("ASC"); //����ָ��������˳��д��order_index
								order_index.add(tempt);
							}
							else if((orderlist.get(i+1).equals("desc"))||(orderlist.get(i+1).equals("DESC"))){
								List<String> tempt=new ArrayList<>();
								tempt.add(String.valueOf(j));
								tempt.add("DESC"); //����ָ��������˳��д��order_index
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
				System.out.println("���ڵ����������±����������б�Ϊ��"+order_index);
				for(int i=0;i<order_index.size();i++){
					//�������е��������У���ÿһ���������ж���������������
					if(order_index.get(i).get(1).equals("ASC")){ //���������
						int pos=Integer.valueOf(order_index.get(i).get(0)); //ȡ�������ֶζ�Ӧ���±�
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
					else if(order_index.get(i).get(1).equals("DESC")){ //����ǽ���
						int pos=Integer.valueOf(order_index.get(i).get(0)); //ȡ�������ֶζ�Ӧ���±�
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
				System.out.println("�����Ժ�Ľ��Ϊ��"+current_list);
				/*if(idx==0){
					return;
				}*/
			}
			idx-=1;
		}
		System.out.println("���յ�outputfieldΪ"+outputfield);
		System.out.println("���յ�current_listΪ"+current_list);
		//return;
	}
	public void execute_logical_tree(List<String> table_name_list) throws IOException{
		if(Node.global_logical_tree!=null){
			List<String> outputfield=new ArrayList<>();
			List<List<String>> current_list=new ArrayList<>();
			Boolean isright=false;
			execute_tree(table_name_list,Node.global_logical_tree);
			if(isright){
				System.out.println("������ֶ�Ϊ��");
				System.out.println(outputfield);
				System.out.println("��ѯ���Ϊ��");
				for(int i=0;i<current_list.size();i++){
					System.out.println(current_list.get(i));
				}
			}
			else{
				System.out.println("�����sql������룡");
			}
			
		}
		else{
			System.out.println("�ò�ѯû����");
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
			//where_list�Ȳ�����
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
			System.out.println("û���������﷨����");
		}
	}
	
	
	
}
