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
		//System.out.println("--------���ڿ�ʼִ����ȡsfw���ݲ���------");
		if(syn_tree==null){
			System.out.println("����");
			return null;
		}
		else{
			System.out.println("�����﷨����Ϊ��");
			ParseNode2 PN=new ParseNode2();
			destruct(syn_tree,PN); //��������﷨�������sellist,fromlist,wherelist,orderlist
			return PN;
		}
	}
	//tmplist���汣�����nodeobj��valueֵ ����ʵӦ�þ�����Ҫ�ҳ���ÿ��sellist,fromlist,wherelist����������ԡ���ϵ����
	/*public void show(Node nodeobj,List<Object> tmplist){
		//nodeobj.show(nodeobj);
		Node node=nodeobj;
		node.show(node);
		if(nodeobj.children==null){ //��ǰ�ڵ�ΪҶ�ڵ�
			System.out.println("���ڽ����˺��ӽڵ�");
			Object o1=nodeobj.value;
			tmplist.add(o1);
		}
		else{
			for(int i=0;i<nodeobj.children.size();i++){
				show(nodeobj.children.get(i),tmplist);
			}
		}
	}*/
	//���﷨������sellist,fromlist,wherelist,orderlist
	public void destruct(Node nodeobj,ParseNode2 PN){
		if(nodeobj.children!=null){
			if(nodeobj.value=="SelList"){
				nodeobj.show(nodeobj);
				List<Object> tmplist=new ArrayList<>();
				nodeobj.get_child(nodeobj,tmplist); //�õ�����ڵ��Ӧ������������Ҷ�ڵ��ֵ
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
		if(fromlist.size()!=0){ //fromlist�б�Ϊ��
			if(fromlist.size()==1){ //ֻ��һ������
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
				//ÿ��ѡ�����һ��Ԫ��ֵȥ����һ��Ҷ�ڵ�
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
		Node.traver(selnode, sqlsent); //sqlsent������selnode���������нڵ��valueֵ
		//System.out.println(sqlsent);
		if(!sqlsent.contains("Distinct")){
			return selnode;
		}
		else{
			List<Node> templist=new ArrayList<>();
			templist.add(selnode);
			node=new Node("Distinct",templist,null); //��selnode��ΪDistinct�ڵ�ĺ���
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
	//���ڹ�������dict,��ͬidx�ڵ��dict[idx]���Ͳ�ͬ
	public void build_execute_tree(Node nodeobj,int idx,HashMap<Integer,List<List<Object>>> dict){
		if(!dict.containsKey(idx)){ //��û�������ֵ����ʱ��Ҫ��ʼ��һ�����б�
			dict.put(idx, new ArrayList<>()); //��ʼ��Ϊ���б�
		}
		//System.out.println("ԭ����dict[where]"+dict.get(idx));
		List<Object> l1=new ArrayList<>(); 
		//System.out.println("�����nodeobj��valueΪ"+nodeobj.value);
		Object o1=nodeobj.value;
		l1.add(o1); //���Ƚ���valueΪ��������б�
		dict.get(idx).add(l1);
		if(nodeobj.var!=null){
			//System.out.println("�����var��Ϊ��");
			int len=dict.get(idx).size();
			List<Object> list=new ArrayList<>();
			//System.out.println("����var�б�Ϊ"+nodeobj.var);
			Object o2=nodeobj.var; //��nodeobj.varת���object����
			Object o3=dict.get(idx).get(len-1); //��ԭ�������һ��Ԫ��Ҳת���object����
			list.add(o3);
			list.add(o2);
		//	System.out.println(list);
			dict.get(idx).set(len-1, list);
			/*dict.get(idx).remove(len-1); //ɾ��ԭ�������һ��Ԫ��
			dict.get(idx).add(list);*/
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
		List<List<String>> result=new ArrayList<>();
		while(input.size()>1){ //�����ȴ���1
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
		int idx=0;
		HashMap<Integer,List<List<Object>>> dict=new HashMap<>();
		build_execute_tree(nodeobj,idx,dict);
		
		Set<Integer> keys=dict.keySet(); //��ȡdict�ļ�ֵset
		List<Integer> key_list=new ArrayList<Integer>(keys); //setת����list 
		//��key_List��������
		Collections.reverse(key_list); //��key_list��ת
		int fromidx=key_list.get(0);		
		idx=key_list.get(0); //��ʼ����idxӦ��Ϊ��ײ�from
		//System.out.println("���ڵ�idxΪ"+idx);
		List<List<String>> current_field=new ArrayList<>();
		List<String> tablename_order=new ArrayList<>();
		
		List<List<String>> fieldnamelist=new ArrayList<>();
		List<List<List<String>>> recordlist=new ArrayList<>();
		List<String> fromnamelist=new ArrayList<>();
		List<String> allsel_field=new ArrayList<>(); //�洢������select��where������ֶ�
		while(idx>=0){
			//System.out.println("���ڵ�idxΪ��"+idx);
			if(idx==fromidx){
				isright=true;
				//System.out.println("-------------------------------------------");
				//System.out.println("����ִ�� ������ײ�from�ڵ�");
				//System.out.println("---------"+dict.get(idx)+"---------");
				if(dict.get(idx).size()>1){  //���fromlist����>1
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
					Object o1=dict.get(idx).get(0).get(0);
					Object o2=dict.get(idx).get(1).get(0);
					String tablename1=o1.toString();
					String tablename2=o2.toString();
					Storage dataobj1=new Storage(tablename1); //�����������ݶ���
					Storage dataobj2=new Storage(tablename2);
					
					List<List<String>> recordlist1=dataobj1.record_list;
					List<List<String>> recordlist2=dataobj2.record_list;
					Stream<List<String>> st1=dataobj1.field_name_list.stream();
					List<String> fieldnamelist1=st1.map((x)->x.get(0)).collect(Collectors.toList());
					
					Stream<List<String>> st2=dataobj2.field_name_list.stream();
					List<String> fieldnamelist2=st2.map((x)->x.get(0)).collect(Collectors.toList());
					
					fieldnamelist.add(fieldnamelist1);
					fieldnamelist.add(fieldnamelist2);
					recordlist.add(recordlist1);  //�����ű��������Ϣ�����ܵ�������Ϣ�б�
					recordlist.add(recordlist2);
					fromnamelist.add(tablename1);
					fromnamelist.add(tablename2);
				}
				else{ //ֻ��һ�ű�
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
				//System.out.println("�ײ�from����ļ�¼�б�Ϊ"+recordlist);
				//System.out.println("�ײ�from����������б�Ϊ"+fieldnamelist);
			}
			else if((dict.get(idx).get(0).contains("From"))&&(dict.get(idx).size()>1)){
				//ֻ���е����ű��ʱ��Ż�ִ�������֧ 
				//System.out.println("--------------------------------");
				//System.out.println("����ִ�е����ϲ��from");
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
				//System.out.println("�ϲ�from����ļ�¼�б�Ϊ"+recordlist);
				//System.out.println("�ϲ�from����������б�Ϊ"+fieldnamelist);
			}
			else if((dict.get(idx).get(0).contains("From"))&&(dict.get(idx).get(0).size()==1)){
				idx-=1;
				continue;
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Where")){
				isright=true;
				//System.out.println("--------------------------------");
				//System.out.println("����ִ�е���where");
				//System.out.println(((List)dict.get(idx)));
				Object condlist=dict.get(idx).get(0).get(1); //��ȡwhere������
				List<List<String>> filter=(List)condlist;
				//System.out.println(filter);
				for(int i=0;i<filter.size();i++){
					List<String> cond=filter.get(i); //��ȡ��i������
					//System.out.println("��"+i+"������"+cond);
					if("=".equals(cond.get(1))){ //���е��ǵ�ֵ�ж�
						String ch1=cond.get(0);
						String ch2=cond.get(2);
						if((ch1.contains("."))&&(ch2.contains("."))){
							//������е��ǵ�ֵ���� ,����������������Ϊ��Ȼ���Ӿ��������ֵ���ӵ�
							allsel_field.add(ch1); //�ѵ�ֵ���ӵ��ֶμ���allsel_field
							allsel_field.add(ch2);
							continue;
						}
						else{ //�Ǽ򵥵ĵ�ֵ�жϣ��Ⱥ�������ֶ������ұ��ǳ���
							//System.out.println("���ڽ��е��ǵ�ֵ�ж�����"+cond);
							List<List<Integer>> ind=new ArrayList<>();
							for(int j=0;j<fieldnamelist.size();j++){ //����ÿ����������б�
								for(int k=0;k<fieldnamelist.get(j).size();k++){
									if(ch1.equals(fieldnamelist.get(j).get(k).trim())){
										List<Integer> temp_ind=new ArrayList<>();
										temp_ind.add(j);
										temp_ind.add(k); //������������ֶ����ڵ�λ�ã��ĸ�����Ǹ��±���ֶ��ϣ�
										ind.add(temp_ind);
										break;
									}
								}
								/*if(ind.size()!=0){ //��ʾ�Ѿ��ҵ�������ֶζ�Ӧ���±꣬����ı��������Ϣ����Ҫ�ٿ�
									break;
								}*/
							}
							//System.out.println(ind);
							//List<List<List<String>>> temp_record=new ArrayList<>();
							if(ind.size()!=0){ //�Ҵ�Сֻ��Ϊ1����Ϊ��ֵ�жϵ��ֶ�һ����һ������ֶΣ���������ڶ��ű�������ͻ��ǵ�ֵ�����ˣ���
								//System.out.println(ch1+"�ֶζ�Ӧ���±�"+ind);
								for(int t=0;t<ind.size();t++){
									
									List<List<String>> temp_recordlist=recordlist.get(ind.get(t).get(0)); //��ȡ���ֶζ�Ӧ�ı�ļ�¼
									List<List<String>> new_recordlist=new ArrayList<>(); //�洢���ֶζ�Ӧ�ı�������ֵ�ж������ļ�¼
									for(int j=0;j<temp_recordlist.size();j++){
										List<String> re=temp_recordlist.get(j); //����ȡÿ����¼
										if(re.get(ind.get(t).get(1)).trim().equals(ch2)){ //���������¼�ĸ��ֶ��ϵ�ȡֵ��������ĳ�����ȣ����ʾ�ü�¼ֵ�����ֵ�ж������ļ�¼
											new_recordlist.add(re);
										}
									}
									//temp_record.add(new_recordlist); 
									recordlist.set(ind.get(t).get(0), new_recordlist);
									/*recordlist.remove(ind.get(t).get(0)); //ɾ��ԭ���ñ�ļ�¼�б�
									recordlist.add(ind.get(t).get(0), new_recordlist); //�����µ������ֵ�ж������ĸñ�ļ�¼�б�
*/									//allsel_field.add(ch1); //�������ֵ�ж��������ֶμ����б�Ϊ��֮��ͶӰ��׼��
								}
								//System.out.println("����������Ǿ�����ֵ�ж������жϵ����������ļ�¼");
								//System.out.println(recordlist);
							}
							else{
								System.out.println("��ֵ�жϵ��ֶβ����ڣ�����");
								outputfield=new ArrayList<>();
								current_list=new ArrayList<>();
								isright=false;
								return;
							}
						}
					}
					else if("LIKE".equals(cond.get(1))||"like".equals(cond.get(1))){
						//������е��������Բ�ѯ
						String ch1=cond.get(0);
						String ch2=cond.get(2);
						//System.out.println("����ִ�е��������Բ�ѯ");
						List<List<Integer>> ind=new ArrayList<>();
						for(int j=0;j<fieldnamelist.size();j++){
							List<Integer> temp=new ArrayList<>();
							for(int k=0;k<fieldnamelist.get(j).size();k++){
								if(ch1.equals(fieldnamelist.get(j).get(k).trim())){//����������������Ǹñ�����ԣ�����¸����Եĳ���λ��
									temp.add(j);
									temp.add(k);
									ind.add(temp);
								}
							}
						}
						if(ind.size()!=0){ //��ʾ�����������
							for(int j=0;j<ind.size();j++){
								//�ҵ��ñ�ļ�¼�б�
								List<List<String>> new_recordlist=new ArrayList<>();
								List<List<String>> temp_recordlist=recordlist.get(ind.get(j).get(0));
								for(int k=0;k<temp_recordlist.size();k++){ //�����ñ��ÿ����¼
									String fie=temp_recordlist.get(k).get(ind.get(j).get(1)).trim(); //��ȡ�ü�¼�ڸ������ϵ�ȡֵ
									if(ch2.startsWith("%")){ //���%�ڿ�ͷ����ƥ����...��β��Ԫ��
										String subch2=ch2.substring(1);
										int ch2len=subch2.length();
										if(fie.substring(fie.length()-ch2len).equals(subch2)){  //�������ü�¼�ĸ����Ե�ĩβȷʵ�Ǹ����ĳ���
											new_recordlist.add(temp_recordlist.get(k)); 
										}
									}
									else if(ch2.endsWith("%")){ //���%��ĩβ����ʾƥ����...��ͷ��Ԫ��
										String subch2=ch2.substring(0,ch2.length()-1); //ȡǰ���ƥ�䲿��
										int ch2len=subch2.length();
										if(fie.substring(0,ch2len).equals(subch2)){ //�������ü�¼�ĸ����Ե�ǰ��ȷʵ��ָ���ĳ���
											new_recordlist.add(temp_recordlist.get(k));
										}
									}
									else if((ch2.indexOf("%")!=0)||(ch2.indexOf("%")!=ch2.length()-1)){
										//���%���������ڿ�ͷ�ͽ�β
										//System.out.println("����ƥ���%���м�");
										String subch21=ch2.substring(0, ch2.indexOf("%"));
										int len1=subch21.length();
										String subch22=ch2.substring(ch2.indexOf("%")+1);
										int len2=subch22.length();
										if((fie.substring(0,len1).equals(subch21))&&(fie.substring(fie.length()-len2).equals(subch22))){
											new_recordlist.add(temp_recordlist.get(k));
										}
									}
										
									
								}
								recordlist.set(ind.get(j).get(0), new_recordlist); //�滻��ԭ���ļ�¼�б�
							}
						}
						else{
							System.out.println("��ѯ���ֶβ����ڣ�����");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
					}
					else if("<".equals(cond.get(1))){
						String ch1=cond.get(0);
						String ch2=cond.get(2);
						//System.out.println("���ڽ��е���С���ж�����"+cond);
						List<List<Integer>> ind=new ArrayList<>();
						for(int j=0;j<fieldnamelist.size();j++){ //����ÿ����������б�
							for(int k=0;k<fieldnamelist.get(j).size();k++){
								if(ch1.equals(fieldnamelist.get(j).get(k).trim())){
									List<Integer> temp_ind=new ArrayList<>();
									temp_ind.add(j);
									temp_ind.add(k); //������������ֶ����ڵ�λ�ã��ĸ�����Ǹ��±���ֶ��ϣ�
									ind.add(temp_ind);
									break;
								}
							}
						}
						//System.out.println(ind);
						if(ind.size()!=0){ //�Ҵ�Сֻ��Ϊ1����Ϊ��ֵ�жϵ��ֶ�һ����һ������ֶΣ���������ڶ��ű�������ͻ��ǵ�ֵ�����ˣ���
							//System.out.println(ch1+"�ֶζ�Ӧ���±�"+ind);
							for(int t=0;t<ind.size();t++){
								
								List<List<String>> temp_recordlist=recordlist.get(ind.get(t).get(0)); //��ȡ���ֶζ�Ӧ�ı�ļ�¼
								List<List<String>> new_recordlist=new ArrayList<>(); //�洢���ֶζ�Ӧ�ı�������ֵ�ж������ļ�¼
								for(int j=0;j<temp_recordlist.size();j++){
									List<String> re=temp_recordlist.get(j); //����ȡÿ����¼
									if(re.get(ind.get(t).get(1)).trim().compareTo(ch2)<0){ //���������¼�ĸ��ֶ��ϵ�ȡֵС��������ĳ��������ʾ�ü�¼ֵ�����ֵ�ж������ļ�¼
										new_recordlist.add(re);
									}
								}
								recordlist.set(ind.get(t).get(0), new_recordlist);
							}
							//System.out.println("����������Ǿ���С���ж������жϵ����������ļ�¼");
							//System.out.println(recordlist);
						}
						else{
							System.out.println("�����ڸ��ֶΣ�����");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
					}
					else if(">".equals(cond.get(1))){
						String ch1=cond.get(0);
						String ch2=cond.get(2);
						//System.out.println("���ڽ��е��Ǵ����ж�����"+cond);
						List<List<Integer>> ind=new ArrayList<>();
						for(int j=0;j<fieldnamelist.size();j++){ //����ÿ����������б�
							for(int k=0;k<fieldnamelist.get(j).size();k++){
								if(ch1.equals(fieldnamelist.get(j).get(k).trim())){
									List<Integer> temp_ind=new ArrayList<>();
									temp_ind.add(j);
									temp_ind.add(k); //������������ֶ����ڵ�λ�ã��ĸ�����Ǹ��±���ֶ��ϣ�
									ind.add(temp_ind);
									break;
								}
							}
						}
						//System.out.println(ind);
						if(ind.size()!=0){ //�Ҵ�Сֻ��Ϊ1����Ϊ��ֵ�жϵ��ֶ�һ����һ������ֶΣ���������ڶ��ű�������ͻ��ǵ�ֵ�����ˣ���
							//System.out.println(ch1+"�ֶζ�Ӧ���±�"+ind);
							for(int t=0;t<ind.size();t++){
								
								List<List<String>> temp_recordlist=recordlist.get(ind.get(t).get(0)); //��ȡ���ֶζ�Ӧ�ı�ļ�¼
								List<List<String>> new_recordlist=new ArrayList<>(); //�洢���ֶζ�Ӧ�ı�������ֵ�ж������ļ�¼
								for(int j=0;j<temp_recordlist.size();j++){
									List<String> re=temp_recordlist.get(j); //����ȡÿ����¼
									if(re.get(ind.get(t).get(1)).trim().compareTo(ch2)>0){ //���������¼�ĸ��ֶ��ϵ�ȡֵС��������ĳ��������ʾ�ü�¼ֵ�����ֵ�ж������ļ�¼
										new_recordlist.add(re);
									}
								}
								recordlist.set(ind.get(t).get(0), new_recordlist);
							}
							//System.out.println("����������Ǿ���С���ж������жϵ����������ļ�¼");
							//System.out.println(recordlist);
						}
						else{
							System.out.println("�����ڸ��ֶΣ�����");
							outputfield=new ArrayList<>();
							current_list=new ArrayList<>();
							isright=false;
							return;
						}
					}
				}
				//System.out.println("����whereɸѡ��¼�б�Ϊ"+recordlist);
				//System.out.println("����whereɸѡ�������б�Ϊ"+fieldnamelist);
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Sel")){
				isright=true;
				//System.out.println("--------------------------------");
				//System.out.println("����ִ�е���select");
				Object obj=dict.get(idx).get(0).get(1);
				List<String> select=(List)obj; //��ȡsellist
				//System.out.println("���ڵ�select�б�Ϊ"+select);
				if(select.get(0).contains("*")){ //�������*�ŵĻ�����ʾfrom�б�ֻ��һ�������ñ�������ֶξ�����
					for(int j=0;j<fieldnamelist.get(0).size();j++){
						allsel_field.add(fieldnamelist.get(0).get(j));
					}
				}
				else{
					for(int i=0;i<select.size();i++){
						allsel_field.add(select.get(i)); //��sellist��������е��ֶζ��ӵ�allsel_field����
					}
				}
				
				HashMap<Integer,List<Integer>> map=new HashMap<>(); //�洢ÿ�����Լ��ñ�Ҫ�������ֶε��±�
				for(int i=0;i<fromnamelist.size();i++){ //����ÿ�ű�
					List<Integer> temp=new ArrayList<>(); //�洢���ű�Ҫ�������ֶε��±�
					for(int j=0;j<allsel_field.size();j++){ //��������Ҫ�������ֶ�
						if(!allsel_field.get(j).contains(".")){ //����Ĳ�����ͬ�ֶ�
							for(int k=0;k<fieldnamelist.get(i).size();k++){ //�������ű���ֶ��б�
								if(allsel_field.get(j).equals(fieldnamelist.get(i).get(k).trim())){
									temp.add(k);
									//allsel_field.remove(j);
								}
							}
						
						}
						else{ //����ͬ�ֶ�
							String[] str=allsel_field.get(j).split("\\.");
							String table1=str[0];
							String field1=str[1];
							if(table1.equals(fromnamelist.get(i).trim())){//�ǵ�ǰ����ֶ�
								for(int k=0;k<fieldnamelist.get(i).size();k++){
									if(field1.equals(fieldnamelist.get(i).get(k).trim())){//�ҵ����ֶε��±�
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
				for(int i=0;i<keylist.size();i++){ //����ÿ�ű�
					List<List<String>> new_record=new ArrayList<>(); //����ñ���µļ�¼����ͶӰ�Ժ�ļ�¼��
					List<Integer> li=map.get(i);  //��ǰ�ñ�ͶӰ���ֶε��±��б�
					
					for(int k=0;k<recordlist.get(i).size();k++){ //�����ñ��ÿ����¼
						List<String>  temp=new ArrayList<>();
						for(int j=0;j<li.size();j++){ //ȡ������¼����Щ�ֶ��ϵ�ֵ
							temp.add(recordlist.get(i).get(k).get(li.get(j)));
						}
						new_record.add(temp);
					}
					recordlist.remove(i);
					recordlist.add(i,new_record); //�����µļ�¼�б�
					
					List<String> new_field=new ArrayList<>(); //�����µ� �����б�ֻ����ÿ����ͶӰ�������ֶΣ�
					for(int k=0;k<li.size();k++){
						new_field.add(fieldnamelist.get(i).get(li.get(k)));
					}
					fieldnamelist.remove(i);
					fieldnamelist.add(i,new_field); //�����µ������б�
				}
				//System.out.println("����������Ǿ���ͶӰ�Ժ�ļ�¼�б�");
				//System.out.println(recordlist);
				//System.out.println(fieldnamelist);
				//System.out.println("----------------------------");
				List<List<String>> result=new ArrayList<>();
				if(recordlist.size()>1){
					result=nature_link(recordlist,fieldnamelist);
					//System.out.println("��Ȼ���ӵĽ��Ϊ��"+result);
					//System.out.println("+++++++++++++++++++++++++++++");
					for(int i=0;i<fieldnamelist.size();i++){
						for(int j=0;j<fieldnamelist.get(i).size();j++){
							if(!outputfield.contains(fieldnamelist.get(i).get(j))){
								outputfield.add(fieldnamelist.get(i).get(j)); //�������Ƿ��ظ�������
							}
						}
					}
				}
				else{
					result=recordlist.get(0);
					//System.out.println("������Ϊ"+result);
					outputfield=fieldnamelist.get(0);
				}
				//System.out.println("�����������outputfield"+outputfield);
				//System.out.println("--------------------------");
				//����ӻ����Ȼ���ӵĽ���У�ͶӰ��select����ѡ�����ֶζ�Ӧ����Ϣ
				List<List<String>> temp_recordlist=result;
				List<Integer>  temp=new ArrayList<>(); //��¼select�����ֶ�����Ȼ�����Ժ�ļ�¼�б���±�
				result=new ArrayList<>();
				for(int i=0;i<select.size();i++){
					for(int j=0;j<outputfield.size();j++){
						if(select.get(i).trim().equals(outputfield.get(j).trim())){
							temp.add(j); //����ѡ�����ֶ����������±�
						}
					}
				}
				for(int i=0;i<temp_recordlist.size();i++){
					List<String> temp_re=new ArrayList<>();
					for(int j=0;j<temp.size();j++){
						temp_re.add(temp_recordlist.get(i).get(temp.get(j)));
					}
					result.add(temp_re); //��������Ľ����current_list
				}
				current_list=result;
				outputfield=select;
				//System.out.println("�������select���Ľ����");
				//System.out.println(current_list);
				//System.out.println(outputfield);
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Distinct")){
				//System.out.println("--------------------------------");
				//System.out.println("����ִ�е���distinct");
				isright=true;
				List<List<String>> temp_current_list=new ArrayList<>();
				temp_current_list=current_list;
				current_list=new ArrayList<>();
				for(int i=0;i<temp_current_list.size();i++){
					if(!current_list.contains(temp_current_list.get(i))){ //û�г�����temp_current_list����ļ�¼�ż������ڵ�current_list
						current_list.add(temp_current_list.get(i));
					}
				}
				//System.out.println("�������distinct���Ľ����");
				//System.out.println(current_list);
				//System.out.println(outputfield);
			}
			else if(((List)dict.get(idx).get(0).get(0)).contains("Order")){
				//System.out.println("--------------------------------");
				//System.out.println("����ִ�е���order");
				isright=true;
				List<List<String>> order_index=new ArrayList<>();
				List<String> orderlist=new ArrayList<>();
				orderlist=(List)dict.get(idx).get(0).get(1);
				//System.out.println("���ڵ������б�Ϊ��"+orderlist);
				for(int i=0;i<orderlist.size();i++){ //������Ϊdict����Ĵ�Ľṹ��["f1","asc","f2"]�����ģ����ֶ��Ƿ���һ���б����
					String recordfield=orderlist.get(i).trim();
					if((recordfield=="asc")||(recordfield=="desc")||(recordfield=="ASC")||(recordfield=="DESC")){
						continue;
					}
					if(!outputfield.contains(recordfield)){
						//�����Ҫ������ֶ�û�г���������ֶ��������
						//System.out.println("�����ֶδ������������������ֶ��������");
						outputfield=new ArrayList<>();
						current_list=new ArrayList<>();
						isright=false;
						return;
					}
					for(int j=0;j<outputfield.size();j++){ //����ÿ���������
						if(outputfield.get(j).trim().equals(recordfield.trim())){ //��������ֶΣ���������ֶκ͵�ǰ�����ֶ���ͬ
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
				//System.out.println("���ڵ����������±����������б�Ϊ��"+order_index);
				for(int i=0;i<order_index.size();i++){
					//�������е��������У���ÿһ���������ж���������������
					if(order_index.get(i).get(1).equals("ASC")){ //���������
						int pos=Integer.valueOf(order_index.get(i).get(0)); //ȡ�������ֶζ�Ӧ���±�
						Collections.sort(current_list,new Comparator<List<String>>(){
							@Override
							public int compare(List<String> l1,List<String> l2){
								return l1.get(pos).compareTo(l2.get(pos));
							}
						});
					}
					else if(order_index.get(i).get(1).equals("DESC")){ //����ǽ���
						int pos=Integer.valueOf(order_index.get(i).get(0)); //ȡ�������ֶζ�Ӧ���±�
						Collections.sort(current_list,new Comparator<List<String>>(){
							@Override
							public int compare(List<String> l1,List<String> l2){
								return l2.get(pos).compareTo(l1.get(pos));
							}
						});
					}
				}
				//System.out.println("�����Ժ�Ľ��Ϊ��"+current_list);
			}
			idx-=1;
		}
		System.out.println("���յĽ���б�Ϊ��"+current_list);
		System.out.println("���յ��ֶ�Ϊ��"+outputfield);
		isright=true;
	}
	
	public void execute_logical_tree(List<String> table_name_list) throws IOException{
		if(Node.global_logical_tree!=null){
			//System.out.println("�߼����Ѿ������");
			Node.global_logical_tree.show(Node.global_logical_tree);
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
	
	public void construct_logical_tree() throws IOException{
		if(syn_tree!=null){
			System.out.println("������������﷨��");
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
				for(int j=0;j<pos;j++){ //��and֮ǰ�ķ���һ���б���ȥ��Ϊһ������
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
			
			//System.out.println("����sellist"+sel_list);
			//System.out.println("����fromlist"+from_list);
			//System.out.println("����wherelist"+where_list);
			//System.out.println("����orderlist"+order_list);
			
			Node from_node=construct_from_node(from_list);
			
			Node where_node=construct_where_node(from_node,where_list);
			Node select_node=construct_select_node(where_node,sel_list);
			Node distinct_node=construct_distinct_select_node(select_node);
			Node.global_logical_tree=construct_order_node(distinct_node,order_list);
			if(Node.global_logical_tree!=null){
				System.out.println("������������߼���");
				Node.show(Node.global_logical_tree);
				System.out.println("----------------");
			}
			Schema obj=new Schema(); //����һ��ģʽ����
			List<String> table_name_list=obj.get_tablenamelist(); //��ȡ�����б�
			execute_logical_tree(table_name_list); //ִ�в�ѯ����ò�ѯ���
		}
		else{
			System.out.println("û���������﷨����");
		}
	}
	
}
