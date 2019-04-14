package xmj.minibase01;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Index {
	public static final int BLOCK_SIZE=4096;
	public static final int MAX_NUM_OF_KEYS=3;
	public static final int LEAF_NODE_TYPE=1;
	public static final int LEN_OF_LEAF_NODE=10+4+4;
	public static final int INTERNAL_NODE_TYPE=0;
	public int SPECIAL_INDEX_BLOCK_PTR=-1;
	public String path="D:\\minibase_data\\index\\";
	public int open;
	public Boolean has_root;
	public int num_of_levels;
	public int root_node_ptr;
	public int num_of_blocks;
	public String filename;
	public RandomAccessFile file;
	public HashMap<Integer,List<String>> key_dic=new HashMap<>();
	public HashMap<Integer,List<List<Integer>>> ptr_dic=new HashMap<>();
	public HashMap<Integer,List<String>> internal_key_dic=new HashMap<>();
	public HashMap<Integer,List<Integer>> internal_ptr_dic=new HashMap<>();
	
	public Index(String filename) throws IOException{
		this.filename=filename.trim();
		File f=new File(this.path+filename+".ind"); 
		if(!f.exists()){ //�ļ��������򴴽�һ��
			System.out.println("�������ļ�"+this.path+filename+".ind�����ڣ�");
			f.createNewFile();
			this.open=0;
			System.out.println("�ļ�"+this.path+filename+".ind�Ѿ�������");
		}
		file=new RandomAccessFile(f,"rw");
		this.open=1;
		System.out.println("�ļ�"+this.path+filename+".ind�Ѿ��򿪣�");
		
		file.seek(0);
		byte[] buf=new byte[BLOCK_SIZE];
		int mylen=file.read(buf); //��ȡ4096���ֽ�
		if(mylen!=-1){ //�����ļ���������
			file.seek(0);
			int block_id=file.readInt();
			this.has_root=file.readBoolean();
			this.num_of_levels=file.readInt();
			this.root_node_ptr=file.readInt();
			this.num_of_blocks=file.readInt();
			/*System.out.println("��ţ�"+block_id);
			System.out.println("�Ƿ��и���"+this.has_root);
			System.out.println("������"+this.num_of_levels);
			System.out.println("���ڵ�ָ�룺"+this.root_node_ptr);
			System.out.println("������"+this.num_of_blocks);*/
			
			//����鿴���е�������¼
			for(int i=1;i<num_of_blocks;i++){
				file.seek(i*BLOCK_SIZE);
				int blockid=file.readInt();
				int node_type=file.readInt();
				int num_of_keys=file.readInt();
				//System.out.println("��ǰ��ţ�"+blockid);
				
				//System.out.println("��ǰ�ڵ�ļ���Ϊ��"+num_of_keys);
				if(node_type==INTERNAL_NODE_TYPE){ //������ڲ��ڵ�
					//System.out.println("��ǰ�ڵ�Ϊ�ڲ��ڵ�");
					List<String> inter_key_list=new ArrayList<>();
					List<Integer> inter_ptr_list=new ArrayList<>();
					for(int j=0;j<num_of_keys;j++){ //���ζ������ָ��ֱ�����б�
						file.seek(BLOCK_SIZE*i+12+j*14);
						byte[] key=new byte[10];
						file.read(key);
						inter_key_list.add(new String(key));
						int ptr=file.readInt();
						inter_ptr_list.add(ptr);
					}
					file.seek(BLOCK_SIZE*i+BLOCK_SIZE-4);
					int last_ptr=file.readInt();
					inter_ptr_list.add(last_ptr);
					System.out.println("���ڲ� �ڵ�ļ�ֵ�б�Ϊ��"+inter_key_list);
					System.out.println("���ڲ��ڵ��ָ���б�Ϊ��"+inter_ptr_list);
					this.internal_key_dic.put(i,inter_key_list);
					this.internal_ptr_dic.put(i, inter_ptr_list);
				}
				else if(node_type==LEAF_NODE_TYPE){ //�������Ҷ�ӽڵ�
					//System.out.println("��ǰ�ڵ���Ҷ�ڵ�");
					List<String> leaf_key_list=new ArrayList<>();
					List<List<Integer>> leaf_ptr_list=new ArrayList<>();
					for(int j=0;j<num_of_keys;j++){ //���ζ������ָ��ֱ�����б�
						file.seek(BLOCK_SIZE*i+12+j*18);
						byte[] key=new byte[10];
						file.read(key);
						leaf_key_list.add(new String(key));
						int ptr=file.readInt();
						int off=file.readInt();
						List<Integer> temp=new ArrayList<>();
						temp.add(ptr);
						temp.add(off);
						leaf_ptr_list.add(temp);
					}
					file.seek(BLOCK_SIZE*i+BLOCK_SIZE-4);
					List<Integer> temp=new ArrayList<>();
					int last_ptr=file.readInt();
					temp.add(last_ptr);
					temp.add(0);
					leaf_ptr_list.add(temp);
					System.out.println("��Ҷ�ڵ�ļ�ֵ�б�Ϊ��"+leaf_key_list);
					System.out.println("��Ҷ�ڵ��ָ���б�Ϊ��"+leaf_ptr_list);
					this.key_dic.put(i,leaf_key_list);
					this.ptr_dic.put(i,leaf_ptr_list);
				}
			}
			
		}
		
	}
	public Boolean deletedata(){
		if(this.open==1){
			this.open=0;
		}
		this.filename=this.filename.trim();
		File f=new File(this.path+this.filename+".ind");
		if(f.exists()){
			f.delete();
			System.out.println(filename.trim()+"��ɾ���ɹ���");
			return true;
		}
		return false;
	}
	
	//public void create_index(String value)
		
	
	//��ȡָ�����һ������һ��
	public int get_next_block_ptr(String current_key,List<String>index_key_list,List<Integer>index_ptr_list){
		int ret_value=-1;
		if(index_key_list.contains(current_key)){
			//�����ǰ�����ļ�ֵ�б�������ֵ����һ����������һ�����ָ�������ߵ���һ��
			int temp_index=index_key_list.indexOf(current_key);
			ret_value=index_ptr_list.get(temp_index+1);
			
		}
		else{ //û�������ֵ
			for(int i=0;i<index_key_list.size();i++){
				if(current_key.compareTo(index_key_list.get(0))<0){ //��ǰ���ֵ<��ֵ�б�ĵ�һ��ֵ
					ret_value=index_ptr_list.get(0); //�򷵻ص�0��ָ��
					System.out.println("��������ȥ����"+ret_value+"��");
				}
				else if(current_key.compareTo(index_key_list.get(index_key_list.size()-1))>0){ //��ǰ���ֵ>��ֵ�б�����һ��ֵ
					ret_value=index_ptr_list.get(index_ptr_list.size()-1); //�򷵻����һ��ָ��
					System.out.println("��������ȥ����"+ret_value+"��");
				}
				else if((current_key.compareTo(index_key_list.get(i))>0)&&(current_key.compareTo(index_key_list.get(i+1))<0)){
					ret_value=index_ptr_list.get(i+1);
					System.out.println("��������ȥ����"+ret_value+"��");
					
				}
			}
		}
		return ret_value;
	}
	
	//����¼���뵽Ҷ���б���
	public void insert_key_into_leaf_list(String insert_key,List<Integer> ptr_tuple,List<String> key_list,List<List<Integer>> ptr_list){
		int dif=10-insert_key.length();
		String str="";
		for(int i=0;i<dif;i++){
			str+=" ";
		}
		insert_key=str+insert_key;
		int pos;
		if(key_list.size()>0){ //��ֵ�б���>0������ֵ�б�����������
			pos=-1;
			for(int i=0;i<key_list.size();i++){
				String current_key=key_list.get(i);
				if(current_key.trim().compareTo(insert_key.trim())>0){ //��ǰ��ֵ>=Ҫ����ļ�ֵ
					pos=i; //��i��ָ��ָ��<ki�ļ�
					break;
				}
			}
			if(pos==-1){ //Ҫ����ļ�ֵ�ǵ�ǰ��ֵ�б�������
				pos=key_list.size(); //ָ�����һ��λ�ã����������
			}
			key_list.add(pos,insert_key);
			ptr_list.add(pos,ptr_tuple);
		}
		else if(key_list.size()==0){
			key_list.add(insert_key);
			ptr_list.add(ptr_tuple);
		}
		System.out.println("�����Ժ�Ҷ�ڵ��ֵ�б�"+key_list);
		System.out.println("�����Ժ�Ҷ�ڵ�ָ���б�"+ptr_list);
	}
	//����һ��������¼��blockid��Ҫ����ļ�ֵ���ڵ������ļ���Ŀ�ţ�offset�Ǽ�ֵ��Ӧ�ļ�¼�����ݿ����ƫ��
	public void insert_index_entry(String field_value,int block_id,int offset) throws IOException{
		System.out.println("���ڿ�ʼִ�в���һ��������¼");
		int dif=10-field_value.length();
		String str="";
		for(int i=0;i<dif;i++){
			str+=" ";
		}
		field_value=str+field_value;
		file=new RandomAccessFile(this.path+this.filename+".ind","rw");
		file.seek(0);
		
		byte[] buf=new byte[BLOCK_SIZE];
		int mylen=file.read(buf);
		if((field_value.length()>0)&&(block_id>0) ){
			//Ҫ����ļ�ֵ��Ϊ�գ��Ҳ���Ŀ�Ų�Ϊ0
			if(mylen==-1){//�ļ���û������
				//������׼�������ڵ�����ݣ����ڿ�1��
				System.out.println("��ǰ�����ļ�û������");
				//file=new RandomAccessFile(this.path+this.filename+".ind","rw");
				//field_value+=str; //�����ֵ
				file.seek(BLOCK_SIZE); //�ҵ���ӦҪ�����
				file.writeInt(1); //д����
				file.writeInt(LEAF_NODE_TYPE);//д��ڵ�����
				file.writeInt(1); //д�����
				file.write(field_value.getBytes()); //д��key0
				file.writeInt(block_id); //д��Ҷ�ڵ��ptr =��ţ�ƫ��
				file.writeInt(offset);
				file.seek(BLOCK_SIZE*1+BLOCK_SIZE-4);
				file.writeInt(SPECIAL_INDEX_BLOCK_PTR);
				List<String> key_list=new ArrayList<>();
				List<List<Integer>> ptr_list=new ArrayList<>();
				List<Integer> temp=new ArrayList<>();
				temp.add(block_id);
				temp.add(offset);
				key_list.add(field_value); //��key_list����һ��������¼
				ptr_list.add(temp);
				List<Integer> t=new ArrayList<>();
				t.add(SPECIAL_INDEX_BLOCK_PTR);
				t.add(0);
				ptr_list.add(t);
				this.key_dic.put(1, key_list);
				this.ptr_dic.put(1, ptr_list);
				
				//������׼��Ԫ��ڵ㣬���ڿ�0��
				file.seek(0);
				file.writeInt(0); //д����
				file.writeBoolean(true); //д���Ƿ��и�
				file.writeInt(1); //д�����
				file.writeInt(1); //д����ڵ�ָ�루��ţ�
				file.writeInt(2);
				
				this.has_root=true;
				this.num_of_levels=1;
				this.root_node_ptr=1;
				this.num_of_blocks=2; //��ǰ����
				file.close();
				System.out.println("�Ѿ����������ɹ���");
			}
			else{ //����Ϣ�������ļ���,��ô�Ӹ���ʼ��һֱ�����ҵ�Ӧ�ò����Ҷ�ڵ�
				System.out.println("�������ļ�������");
				file=new RandomAccessFile(this.path+this.filename+".ind","rw");
				file.seek(0);
				int temp_blockid=file.readInt();
				this.has_root=file.readBoolean();
				this.num_of_levels=file.readInt();
				this.root_node_ptr=file.readInt();
				this.num_of_blocks=file.readInt();
				System.out.println("��ţ�"+temp_blockid);
				System.out.println("�Ƿ��и���"+this.has_root);
				System.out.println("������"+this.num_of_levels);
				System.out.println("������"+this.num_of_blocks);
				if((this.has_root==true)&&(this.num_of_levels>0)&&(this.root_node_ptr>0)){	
					int temp_count=0;
					List<Integer> inherit_list=new ArrayList<>();
					int next_node_ptr=this.root_node_ptr; //��һ���ڵ�Ϊ���ڵ�
					inherit_list.add(next_node_ptr); 
					
					while(temp_count<this.num_of_levels-1){//��û�����������һ��,���϶����ڲ��ڵ�
						System.out.println("�������ڴ����ڲ��ڵ㵽��Ҷ�ڵ�");
						int read_pos=BLOCK_SIZE*next_node_ptr;
						file.seek(read_pos);
						int current_id=file.readInt();
						int current_node_type=file.readInt();
						int current_num_of_keys=file.readInt();
						if(current_node_type!=INTERNAL_NODE_TYPE){
							System.out.println("�ڲ��ڵ����ʹ���");
							return;
						}
						if(current_num_of_keys<=0){
							System.out.println("�ڲ��ڵ�ļ�������");
							return;
						}
						//���������ڲ��ڵ�ļ�ֵ�б��ָ���б�
						List<String> internal_key_list=new ArrayList<>();
						List<Integer> internal_ptr_list=new ArrayList<>();
						for(int i=0;i<current_num_of_keys;i++){
							byte[] current_key=new byte[10];
							file.read(current_key); //��ȡ��ֵ
							internal_key_list.add(new String(current_key)); //�����ڲ��ڵ��ֵ�б�
							int current_ptr=file.readInt(); //��ȡָ��
							internal_ptr_list.add(current_ptr);
						}
						file.seek(read_pos+BLOCK_SIZE-4); //��λ���ÿ�����4�ֽڴ�
						int last_ptr=file.readInt(); //��ȡ���һ��ָ��
						internal_ptr_list.add(last_ptr);
						
						//��Ϊ���ڲ��ڵ㣬����Ҫ�������ڲ��ڵ�������·�������ߣ���Ҫ����¼����Ҷ�ڵ�����Ӧ���ڵ�ǰ�ڲ��ڵ��������ȥ����һ���ڵ㣿
						next_node_ptr=get_next_block_ptr(field_value,internal_key_list,internal_ptr_list);
						if(next_node_ptr==-1)
						{
							System.out.println("��һ���ڵ����");
							return;
						}
						inherit_list.add(next_node_ptr);
						temp_count+=1; //������1����ʾ������һ��
					}
					//���ڴ���Ҷ�ڵ�
					System.out.println("�����Ѿ�����Ҷ�ڵ�");
					int read_pos=BLOCK_SIZE*next_node_ptr;
					file.seek(read_pos);
					int current_id=file.readInt();
					int current_node_type=file.readInt();
					int current_num_of_keys=file.readInt();
					System.out.println("��ǰ��ţ�"+current_id);
					System.out.println("��ǰ�ڵ����ͣ�"+current_node_type);
					System.out.println("��ǰ�ڵ������"+current_num_of_keys);
					file.seek(read_pos+BLOCK_SIZE-4); //ָ�����4�ֽڴ�
					int last_pos=file.readInt();
					if(current_node_type==LEAF_NODE_TYPE){//��ǰ�ڵ���Ҷ�ӽڵ�
						//��������Ҷ�ڵ�ļ�ֵ�б�ָ���б�Ҷ�ڵ��ָ�����������֣�ָ��ü�ֵ��Ӧ�ļ�¼���ڵ����ݿ�Ŀ�ź������ݿ����ƫ��
						List<String> key_list=new ArrayList<>();
						List<List<Integer>> ptr_list=new ArrayList<>();
						for(int i=0;i<current_num_of_keys;i++){
							file.seek(read_pos+12+i*18);
							byte[] current_key=new byte[10];
							file.read(current_key);
							System.out.println("��ǰ��ֵ"+new String(current_key));
							key_list.add(new String(current_key));
							int current_ptr=file.readInt();
							int current_off=file.readInt();
							List<Integer> temp=new ArrayList<>();
							temp.add(current_ptr);
							temp.add(current_off);
							ptr_list.add(temp);
							System.out.println("��ǰָ��"+temp);
						}
						List<Integer> tmp=new ArrayList<>();
						tmp.add(last_pos);
						tmp.add(0);
						ptr_list.add(tmp);
						System.out.println("δ����֮ǰ��ֱ�б�"+key_list);
						System.out.println("δ����֮ǰָ���б�"+ptr_list);
						if(current_num_of_keys<MAX_NUM_OF_KEYS){ //��ǰ���Ҷ�ڵ�ļ�ֵ��<����ֵ�����������Լ��������ֵ
							List<Integer> temp=new ArrayList<>();
							temp.add(block_id); //��Ҫ�����ֵ��ptr
							temp.add(offset);
							insert_key_into_leaf_list(field_value,temp,key_list,ptr_list); //���ò���Ҷ�ڵ㺯��
							System.out.println("�����Ժ�ļ�ֵ"+key_list);
							System.out.println("�����Ժ��ָ��"+ptr_list);
							this.key_dic.put(next_node_ptr, key_list);
							this.ptr_dic.put(next_node_ptr, ptr_list);
							read_pos=next_node_ptr*BLOCK_SIZE;
							//ѭ����key_list��ptr_listд���ļ�
							for(int j=0;j<key_list.size();j++){
								String current_key=key_list.get(j); //��ȡ�ļ�ֵ��10�ֽڵ�
								System.out.println("xieru wenjian��ȡ�ļ�ֵΪ��"+current_key);
								List<Integer> tuple=new ArrayList<>();
								tuple=ptr_list.get(j);
								System.out.println("wenjian��ȡ��ָ��Ϊ��"+tuple);
								file.seek(read_pos+12+j*LEN_OF_LEAF_NODE);
								file.write(current_key.getBytes());
								file.writeInt(tuple.get(0));
								file.writeInt(tuple.get(1));
							}
							current_num_of_keys+=1; //����+1
							last_pos=ptr_list.get(ptr_list.size()-1).get(0);
							file.seek(read_pos);
							file.writeInt(current_id);
							file.writeInt(current_node_type);
							file.writeInt(current_num_of_keys);
							file.seek(read_pos+BLOCK_SIZE-4);
							file.writeInt(last_pos);
						}
						else{
							System.out.println("��ǰҶ�ڵ���������Ҫ����");
							this.num_of_blocks+=1;
							List<Integer> temp=new ArrayList<>();
							temp.add(block_id); //��Ҫ�����ֵ��ptr
							temp.add(offset);
							insert_key_into_leaf_list(field_value,temp,key_list,ptr_list); //���ò���Ҷ�ڵ㺯��
							node_split(key_list,ptr_list,current_id,inherit_list);
							
						}
					}
					else{
						System.out.println("���󣡣���Ӧ����һ��Ҷ�ڵ㣡");
					}
				file.close();
				System.out.println("�Ѿ����������ɹ���");
				}
				else{
					System.out.println("�����ļ�����Ϣ����");
				}
			}
		}
		
	}
	//�ڵ����,current_idָ��ǰ�飨Ҫ���ѵĿ飩
	public void node_split(List<String> key_list,List<List<Integer>> ptr_list,int current_id,List<Integer> inherit_list) throws IOException{
		System.out.println("inherit_listΪ��"+inherit_list);
		System.out.println("------------------------");
		file=new RandomAccessFile(this.path+this.filename+".ind","rw");
		file.seek(current_id*BLOCK_SIZE);
		current_id=file.readInt(); //��ȡ��ǰ���
		int current_node_type=file.readInt(); //��ȡ�ڵ�����
		int current_num_of_keys=file.readInt(); //��ȡ����
		file.seek(current_id*BLOCK_SIZE+BLOCK_SIZE-4);
		int last_ptr=file.readInt(); //��ȡ���һ��ָ��
		file.close();
		if(current_node_type==LEAF_NODE_TYPE){
			file=new RandomAccessFile(this.path+this.filename+".ind","rw");
			//��ǰ�ڵ���Ҷ�ڵ�,���Ѻ����߽ڵ�key����=��n+1��/2��ȡ�����ұ߽ڵ��key����=��n+1��/2��ȡ��
			int num_of_left_leaf=(int) Math.ceil(((float)MAX_NUM_OF_KEYS+1) /2);
			int num_of_right_leaf=(int)Math.floor(((float)MAX_NUM_OF_KEYS+1) /2);
			List<String> left_leaf_key_list=new ArrayList<>();
			List<List<Integer>> left_leaf_ptr_list=new ArrayList<>();
			List<String> right_leaf_key_list=new ArrayList<>();
			List<List<Integer>> right_leaf_ptr_list=new ArrayList<>();
			for(int i=0;i<num_of_left_leaf;i++){
				left_leaf_key_list.add(key_list.get(i));
				left_leaf_ptr_list.add(ptr_list.get(i));
			}
			for(int i=num_of_left_leaf;i<key_list.size();i++){
				right_leaf_key_list.add(key_list.get(i));
				right_leaf_ptr_list.add(ptr_list.get(i));
			}
			System.out.println("��ߵķ��ѽڵ�ļ�ֵ"+left_leaf_key_list);
			System.out.println("�ұߵķ��ѽڵ�ļ�ֵ"+right_leaf_key_list);
			
			file.seek(0);
			int block_id=file.readInt();
			Boolean has_node=file.readBoolean();
			int num_of_level=file.readInt();
			int root_ptr=file.readInt();
			int num_of_block=file.readInt();
			
			int right_id=num_of_block; //ԭ���Ŀ������������֮����ҽڵ㣨����ԭ������Ϊ2�������0��1�������µĿ��Ϊ2��
			num_of_block+=1; //�ڵ�����Ժ󣬻����һ���µĽڵ㣬���Կ���+1(һ���ڵ��Ӧһ����)
			if(num_of_level==1){ //���ֻ��һ�㣬�����һ��+1
				num_of_level+=1;
			}
			file.seek(BLOCK_SIZE*right_id); //ָ�������ɵĿ�
			file.writeInt(right_id);
			file.writeInt(LEAF_NODE_TYPE);
			file.writeInt(num_of_right_leaf);
			for(int i=0;i<num_of_right_leaf;i++){
				file.seek(BLOCK_SIZE*right_id+12+i*18);
				file.write(right_leaf_key_list.get(i).getBytes());
				file.writeInt(right_leaf_ptr_list.get(i).get(0));
				file.writeInt(right_leaf_ptr_list.get(i).get(1));
			}
			file.seek(BLOCK_SIZE*right_id+BLOCK_SIZE-4);
			file.writeInt(last_ptr); //�ұ߷��ѽڵ�����һ��ָ�뻹��ԭ�����ļ������һ��ָ��
			List<Integer> temp=new ArrayList<>();
			temp.add(last_ptr);
			temp.add(0);
			right_leaf_ptr_list.add(temp);
			
			key_dic.put(right_id, right_leaf_key_list);
			ptr_dic.put(right_id, right_leaf_ptr_list);
			
			//��������д������Ժ����ڵ㣬����Ȼ��ԭ���Ŀ���
			file.seek(BLOCK_SIZE*current_id); //ָ��ԭ���Ŀ�
			file.writeInt(current_id);
			file.writeInt(LEAF_NODE_TYPE);
			file.writeInt(num_of_left_leaf); //�����������ļ�������ڵ�ļ���
			for(int i=0;i<num_of_left_leaf;i++){
				file.seek(BLOCK_SIZE*current_id+i*18+12);
				file.write(left_leaf_key_list.get(i).getBytes());
				file.writeInt(left_leaf_ptr_list.get(i).get(0));
				file.writeInt(left_leaf_ptr_list.get(i).get(1));
				
			}
			file.seek(BLOCK_SIZE*current_id+BLOCK_SIZE-4);
			file.writeInt(right_id); //�µĿ����Ϊ�ɿ��last_ptr(�ɿ�����һ��ָ��ָ���¿�)
			List<Integer> tmp=new ArrayList<>();
			tmp.add(right_id);
			tmp.add(0);
			left_leaf_ptr_list.add(tmp);
			key_dic.put(current_id, left_leaf_key_list);
			ptr_dic.put(current_id, left_leaf_ptr_list);
			//����д��Ԫ��Ϣ
			file.seek(0);
			file.writeInt(block_id);
			file.writeBoolean(has_node);
			file.writeInt(num_of_level);
			file.writeInt(root_ptr);
			file.writeInt(num_of_block);
			
			if((inherit_list.size()==1)&&(inherit_list.get(0)==current_id)){
				//ֻ��һ��ڵ㣬����ǰ��û���ڲ��ڵ㣬�����Ψһ��Ҷ�ڵ㣬
				System.out.println("����ִ�е���temp1=[]"+inherit_list);
				List<Integer> temp1=new ArrayList<>();
				//ʹ���ұ߷��ѽڵ�ļ�ֵ�ĵ�һ��Ԫ����Ϊ���ӵ��ڲ��ڵ�ļ�
				System.out.println("temp1"+temp1);
				insert_internal_node(right_leaf_key_list.get(0),current_id,right_id,temp1);
			}
			else{
				System.out.println("����ִ�е���temp=inherit_list[-1]"+inherit_list);
				List<Integer> temp1=new ArrayList<>();
				//temp������inherit_list�ĳ�ȥ���һ��Ԫ�ص��б�
				for(int j=0;j<inherit_list.size()-1;j++){
					temp1.add(inherit_list.get(j));
				}
				System.out.println("temp1="+temp1);
				insert_internal_node(right_leaf_key_list.get(0),current_id,right_id,temp1);
			}
			file.close();
		}
		else if(current_node_type==INTERNAL_NODE_TYPE){//��ǰ�ڵ����ڲ��ڵ�
			file=new RandomAccessFile(this.path+this.filename+".ind","rw");
			int num_left_block=(int)Math.ceil(((float) MAX_NUM_OF_KEYS+1)/2);
			int num_right_block=(int)Math.floor(((float) MAX_NUM_OF_KEYS+1)/2)-1;
			//����key_list��ptr_list��Ϊ���������֣���Ϊ���ҽڵ�
			List<String> left_block_key_list=new ArrayList<>();
			List<Integer> left_block_ptr_list=new ArrayList<>();
			for(int i=0;i<num_left_block;i++){
				left_block_key_list.add(key_list.get(i));
				left_block_ptr_list.add(ptr_list.get(i).get(0));
			}
			left_block_ptr_list.add(ptr_list.get(num_left_block).get(0));
			//�м�Ľڵ㲻����ڵ�Ҳ�����ҽڵ㣬��������һ�㴫�ݣ����Ӧ��ptr��Ϊ��ڵ��last_ptr
			String mid_key=key_list.get(MAX_NUM_OF_KEYS/2+1);
			int mid_ptr=ptr_list.get(MAX_NUM_OF_KEYS/2+1).get(0);
			System.out.println("�м�ڵ�ļ�ֵΪ+"+mid_key);
			System.out.println("�м�ڵ�ָ��Ϊ"+mid_key);
			List<String> right_block_key_list=new ArrayList<>();
			List<Integer> right_block_ptr_list=new ArrayList<>();
			for(int i=num_left_block+1;i<MAX_NUM_OF_KEYS+1;i++){
				right_block_key_list.add(key_list.get(i));
				right_block_ptr_list.add(ptr_list.get(i).get(0));
			}
			right_block_ptr_list.add(ptr_list.get(ptr_list.size()-1).get(0));
			System.out.println("��������ڲ��ڵ��ֵΪ+"+left_block_key_list);
			System.out.println("���Ѻ����ָ���б�Ϊ+"+left_block_ptr_list);
			System.out.println("�����ұ߼�ֵ�б�Ϊ+"+right_block_key_list);
			System.out.println("�����ұ�ָ���б�Ϊ+"+right_block_ptr_list);
			file.seek(0);
			int block_id=file.readInt(); 
			Boolean has_node=file.readBoolean();
			int num_of_level=file.readInt();
			int root_ptr=file.readInt();
			int num_of_block=file.readInt();
			
			int right_id=num_of_block;  //�ҽڵ�Ŀ��=����ӵĿ�ţ������һ����ŵ����ҽڵ���
			num_of_block+=1; //����+1
			if(inherit_list.size()==1){ //������ӹ�ϵֻ��һ�㣬���ʾ�Ѿ�������ڵ㣬���ڵ�������з��ѣ�����Ҫ����һ��
				num_of_level+=1;
			}
			file.seek(BLOCK_SIZE*right_id);
			file.writeInt(right_id);
			file.writeInt(INTERNAL_NODE_TYPE);
			file.writeInt(num_right_block);
			for(int i=0;i<num_right_block;i++){
				file.seek(BLOCK_SIZE*right_id+i*14+12);
				file.write(right_block_key_list.get(i).getBytes());
				file.writeInt(right_block_ptr_list.get(i));
			}
			file.seek(BLOCK_SIZE*right_id+BLOCK_SIZE-4);
			file.writeInt(last_ptr); //�ɽڵ��last_ptr��Ϊ�½ڵ��last_ptr
			
			
			//����д����ڵ�
			file.seek(current_id*BLOCK_SIZE);
			file.writeInt(current_id);
			file.writeInt(INTERNAL_NODE_TYPE);
			file.writeInt(num_left_block);
			for(int i=0;i<num_left_block;i++){
				file.write(left_block_key_list.get(i).getBytes());
				file.writeInt(left_block_ptr_list.get(i));
			}
			file.seek(current_id*BLOCK_SIZE+BLOCK_SIZE-4);
			file.writeInt(mid_ptr);
			
			file.seek(0);
			file.writeInt(block_id);
			file.writeBoolean(has_node);
			file.writeInt(num_of_level);
			file.writeInt(root_ptr);
			file.writeInt(num_of_block);
			file.close();
			
			if((inherit_list.size()==1)&&(inherit_list.get(0)==current_id)){
				//ֻ��һ��ڵ㣬����ǰ��û���ڲ��ڵ㣬�����Ψһ��Ҷ�ڵ㣬
				System.out.println("����ִ�е���temp=[] "+inherit_list);
				List<Integer> temp=new ArrayList<>();
				insert_internal_node(mid_key,current_id,right_id,temp);
			}
			else{
				System.out.println("����ִ�е���temp=inherit_list[-1]"+inherit_list);
				List<Integer> temp=new ArrayList<>();
				//temp������inherit_list�ĳ�ȥ���һ��Ԫ�ص��б�
				for(int j=0;j<inherit_list.size()-1;j++){
					temp.add(inherit_list.get(j));
				}
				insert_internal_node(mid_key,current_id,right_id,temp);
			}
		}
		
	}
	//����һ���ڲ��ڵ�
	public void insert_internal_node(String new_key,int left_ptr,int right_ptr,List<Integer> inherit_list) throws IOException{
		System.out.println("����ִ��һ�������ڲ��ڵ㺯��");
		System.out.println(inherit_list);
		System.out.println("----------------------------");
		file=new RandomAccessFile(this.path+this.filename+".ind","rw");
		file.seek(0);
		int block_id=file.readInt();
		Boolean has_node=file.readBoolean();
		int num_of_level=file.readInt();
		int root_ptr=file.readInt();
		int num_of_block=file.readInt();
		int this_internal_id; //������ӵ��ڲ��ڵ��id
		if(inherit_list.size()==0){//����û�и��ӹ�ϵ��˵��������ӵ��µ�root
			System.out.println("ԭ��û�и��ӹ�ϵ�������¼�һ��root");
			
			this_internal_id=num_of_block; //����ӵ��ڲ��ڵ�Ŀ��
			num_of_block+=1;
			System.out.println("��ǰ�ڵ�Ŀ�ţ�"+this_internal_id);
			root_ptr=this_internal_id; //����ӵĿ���Ǹ��ڵ�
			if(new_key.length()<10){
				String str="";
				for(int i=0;i<10-new_key.length();i++){
					str+=" ";
				}
				new_key=str+new_key;
			}
			file.seek(this_internal_id*BLOCK_SIZE);
			file.writeInt(this_internal_id);
			file.writeInt(INTERNAL_NODE_TYPE);
			file.writeInt(1);
			file.write(new_key.getBytes());
			file.writeInt(left_ptr);
			file.seek(this_internal_id*BLOCK_SIZE+BLOCK_SIZE-4);
			file.writeInt(right_ptr);
			file.seek(0);
			file.writeInt(block_id);
			file.writeBoolean(has_node);
			file.writeInt(num_of_level);
			file.writeInt(root_ptr);
			file.writeInt(num_of_block);
			System.out.println("�ɹ�����һ����");
		}
		else{//ԭ���и��ӹ�ϵ
			System.out.println("ԭ���и��ӹ�ϵ");
			file.seek(inherit_list.get(inherit_list.size()-1)*BLOCK_SIZE); //��������һ�㸸�ף�
			int current_id=file.readInt();
			int current_type=file.readInt();
			int current_num_of_keys=file.readInt();
			file.seek(inherit_list.get(inherit_list.size()-1)*BLOCK_SIZE+BLOCK_SIZE-4);
			int last_ptr=file.readInt();
			if(current_type==INTERNAL_NODE_TYPE){
				List<String> key_list=new ArrayList<>();
				List<List<Integer>> ptr_list=new ArrayList<>();
				for(int i=0;i<current_num_of_keys;i++){
					file.seek(current_id*BLOCK_SIZE+12+i*14);
					byte[] current_key=new byte[10];
					file.read(current_key);
					int current_ptr=file.readInt();
					key_list.add(new String(current_key));
					List<Integer>temp=new ArrayList<>();
					temp.add(current_ptr);
					temp.add(0); //���ڲ��ڵ㣬�ڲ��ڵ��ptrֻ��һ��block_id,��Ҷ�ڵ��ptr�ǣ�block_id,offset��,Ĭ���ڲ��ڵ��offsetΪ0
					ptr_list.add(temp);
				}
				
				//���¼�ֵnew_key����ڵ���ļ�ֵ�б�����뵽���ʵ�λ��
				if(new_key.compareTo(key_list.get(key_list.size()-1))>0){ //��ǰ����ļ�ֵ���ڵ�ǰ�ڵ�������м�ֵ
					List<Integer>temp=new ArrayList<>();
					temp.add(last_ptr);
					temp.add(0);
					ptr_list.add(temp); //ԭ�������һ��ָ�뱻�����µļ�ֵ����ָ��ȡ��
					last_ptr=right_ptr;
					key_list.add(new_key);
				}
				else{
					int pos=-1;
					for(int j=0;j<key_list.size();j++){
						//����key_list�����м�ֵ��ȷ��new_keyӦ�ò�������
						if(new_key.compareTo(key_list.get(j))<0){
							pos=j;
							break;
						}
					}
					key_list.add(pos, new_key);
					List<Integer>temp=new ArrayList<>();
					temp.add(right_ptr);
					temp.add(0);
					ptr_list.add(pos+1,temp);
					
				}
				List<Integer> tmp=new ArrayList<>();
				tmp.add(last_ptr);
				tmp.add(0);
				ptr_list.add(tmp);
				System.out.println("��ǰ��ֵ�б�"+key_list);
				System.out.println("��ǰptr�б�"+ptr_list);
				current_num_of_keys+=1;
				if(current_num_of_keys<=MAX_NUM_OF_KEYS){ //��ǰ����û�е�����󣬲���Ҫ����
					file.seek(current_id*BLOCK_SIZE); //��λ����ǰ�ڵ�
					file.writeInt(current_id);
					file.writeInt(current_type);
					file.writeInt(current_num_of_keys);
					for(int j=0;j<current_num_of_keys;j++){
						file.seek(current_id*BLOCK_SIZE+12+j*14);
						file.write(key_list.get(j).getBytes());
						file.writeInt(ptr_list.get(j).get(0));
					}
					file.seek(current_id*BLOCK_SIZE+BLOCK_SIZE-4);
					file.writeInt(last_ptr);
					
				}
				else{
					node_split(key_list,ptr_list,current_id,inherit_list);
					
				}
			}
		}
		file.close();
	}
	public void show() throws IOException{
		file=new RandomAccessFile(this.path+this.filename+".ind","rw");
		file.seek(0);
		int block_id=file.readInt();
		Boolean has_root=file.readBoolean();
		int levels=file.readInt();
		int rootptr=file.readInt();
		int blocksnum=file.readInt();
		System.out.println("��ǰ��ţ�"+block_id);
		System.out.println("�Ƿ��и���"+has_root);
		System.out.println("������"+levels);
		System.out.println("���ڵ�ָ�룺"+rootptr);
		System.out.println("������"+blocksnum);
		for(int i=1;i<blocksnum;i++){
			file.seek(i*BLOCK_SIZE);
			int id=file.readInt();
			int type=file.readInt();
			int keysnum=file.readInt();
			System.out.println("��ţ�"+id);
			System.out.println("�ڵ����ͣ�"+type);
			System.out.println("������"+keysnum);
			if(type==0){//�ڲ��ڵ�
				List<String> key_list=new ArrayList<>();
				List<Integer> ptr_list=new ArrayList<>();
				for(int j=0;j<keysnum;j++){
					file.seek(i*BLOCK_SIZE+12+j*14);
					byte[]key =new byte[10];
					file.read(key);
					key_list.add(new String(key));
					int p=file.readInt();
					ptr_list.add(p);
				}
				file.seek(i*BLOCK_SIZE+BLOCK_SIZE-4);
				int last_pos=file.readInt();
				ptr_list.add(last_pos);
				System.out.println("�ڲ��ڵ��ֵ�б�"+key_list);
				System.out.println("�ڲ��ڵ�ָ���б�"+ptr_list);
			}
			else if(type==1){//��Ҷ�ڵ�
				
				List<String> key_list=new ArrayList<>();
				List<List<Integer>> ptr_list=new ArrayList<>();
				for(int j=0;j<keysnum;j++){
					file.seek(i*BLOCK_SIZE+12+j*18);
					byte[]key =new byte[10];
					file.read(key);
					key_list.add(new String(key));
					int p=file.readInt();
					int o=file.readInt();
					List<Integer> temp=new ArrayList<>();
					temp.add(p);
					temp.add(o);
					ptr_list.add(temp);
				}
				file.seek(i*BLOCK_SIZE+BLOCK_SIZE-4);
				int last_pos=file.readInt();
				List<Integer> temp=new ArrayList<>();
				temp.add(last_pos);
				temp.add(0);
				ptr_list.add(temp);
				System.out.println("Ҷ�ڵ��ֵ�б�"+key_list);
				System.out.println("Ҷ�ڵ�ָ���б�"+ptr_list);
			}
			
		}
	}
}




