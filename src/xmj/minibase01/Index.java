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
		if(!f.exists()){ //文件不存在则创建一个
			System.out.println("该索引文件"+this.path+filename+".ind不存在！");
			f.createNewFile();
			this.open=0;
			System.out.println("文件"+this.path+filename+".ind已经创建！");
		}
		file=new RandomAccessFile(f,"rw");
		this.open=1;
		System.out.println("文件"+this.path+filename+".ind已经打开！");
		
		file.seek(0);
		byte[] buf=new byte[BLOCK_SIZE];
		int mylen=file.read(buf); //读取4096个字节
		if(mylen!=-1){ //索引文件里有内容
			file.seek(0);
			int block_id=file.readInt();
			this.has_root=file.readBoolean();
			this.num_of_levels=file.readInt();
			this.root_node_ptr=file.readInt();
			this.num_of_blocks=file.readInt();
			/*System.out.println("块号："+block_id);
			System.out.println("是否有根："+this.has_root);
			System.out.println("层数："+this.num_of_levels);
			System.out.println("根节点指针："+this.root_node_ptr);
			System.out.println("块数："+this.num_of_blocks);*/
			
			//下面查看所有的索引记录
			for(int i=1;i<num_of_blocks;i++){
				file.seek(i*BLOCK_SIZE);
				int blockid=file.readInt();
				int node_type=file.readInt();
				int num_of_keys=file.readInt();
				//System.out.println("当前块号："+blockid);
				
				//System.out.println("当前节点的键数为："+num_of_keys);
				if(node_type==INTERNAL_NODE_TYPE){ //如果是内部节点
					//System.out.println("当前节点为内部节点");
					List<String> inter_key_list=new ArrayList<>();
					List<Integer> inter_ptr_list=new ArrayList<>();
					for(int j=0;j<num_of_keys;j++){ //依次读入键、指针分别加入列表
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
					System.out.println("该内部 节点的键值列表为："+inter_key_list);
					System.out.println("该内部节点的指针列表为："+inter_ptr_list);
					this.internal_key_dic.put(i,inter_key_list);
					this.internal_ptr_dic.put(i, inter_ptr_list);
				}
				else if(node_type==LEAF_NODE_TYPE){ //如果是是叶子节点
					//System.out.println("当前节点是叶节点");
					List<String> leaf_key_list=new ArrayList<>();
					List<List<Integer>> leaf_ptr_list=new ArrayList<>();
					for(int j=0;j<num_of_keys;j++){ //依次读入键、指针分别加入列表
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
					System.out.println("该叶节点的键值列表为："+leaf_key_list);
					System.out.println("该叶节点的指针列表为："+leaf_ptr_list);
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
			System.out.println(filename.trim()+"表删除成功！");
			return true;
		}
		return false;
	}
	
	//public void create_index(String value)
		
	
	//获取指向的下一块是哪一块
	public int get_next_block_ptr(String current_key,List<String>index_key_list,List<Integer>index_ptr_list){
		int ret_value=-1;
		if(index_key_list.contains(current_key)){
			//如果当前这个块的键值列表包含这个值，则一定是沿着这一块的右指针往下走到下一块
			int temp_index=index_key_list.indexOf(current_key);
			ret_value=index_ptr_list.get(temp_index+1);
			
		}
		else{ //没有这个键值
			for(int i=0;i<index_key_list.size();i++){
				if(current_key.compareTo(index_key_list.get(0))<0){ //当前这个值<键值列表的第一个值
					ret_value=index_ptr_list.get(0); //则返回第0个指针
					System.out.println("现在向下去到："+ret_value+"块");
				}
				else if(current_key.compareTo(index_key_list.get(index_key_list.size()-1))>0){ //当前这个值>键值列表的最后一个值
					ret_value=index_ptr_list.get(index_ptr_list.size()-1); //则返回最后一个指针
					System.out.println("现在向下去到："+ret_value+"块");
				}
				else if((current_key.compareTo(index_key_list.get(i))>0)&&(current_key.compareTo(index_key_list.get(i+1))<0)){
					ret_value=index_ptr_list.get(i+1);
					System.out.println("现在向下去到："+ret_value+"块");
					
				}
			}
		}
		return ret_value;
	}
	
	//将记录插入到叶子列表中
	public void insert_key_into_leaf_list(String insert_key,List<Integer> ptr_tuple,List<String> key_list,List<List<Integer>> ptr_list){
		int dif=10-insert_key.length();
		String str="";
		for(int i=0;i<dif;i++){
			str+=" ";
		}
		insert_key=str+insert_key;
		int pos;
		if(key_list.size()>0){ //键值列表长度>0，即键值列表里面有内容
			pos=-1;
			for(int i=0;i<key_list.size();i++){
				String current_key=key_list.get(i);
				if(current_key.trim().compareTo(insert_key.trim())>0){ //当前键值>=要插入的键值
					pos=i; //第i个指针指向<ki的键
					break;
				}
			}
			if(pos==-1){ //要插入的键值是当前键值列表里最大的
				pos=key_list.size(); //指向最后一个位置，插入在最后
			}
			key_list.add(pos,insert_key);
			ptr_list.add(pos,ptr_tuple);
		}
		else if(key_list.size()==0){
			key_list.add(insert_key);
			ptr_list.add(ptr_tuple);
		}
		System.out.println("插入以后叶节点键值列表："+key_list);
		System.out.println("插入以后叶节点指针列表："+ptr_list);
	}
	//插入一条索引记录，blockid是要插入的键值所在的数据文件里的块号，offset是键值对应的记录在数据块里的偏移
	public void insert_index_entry(String field_value,int block_id,int offset) throws IOException{
		System.out.println("现在开始执行插入一条索引记录");
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
			//要插入的键值不为空，且插入的块号不为0
			if(mylen==-1){//文件里没有内容
				//下面是准备索引节点的数据，存在块1里
				System.out.println("当前索引文件没有内容");
				//file=new RandomAccessFile(this.path+this.filename+".ind","rw");
				//field_value+=str; //填充域值
				file.seek(BLOCK_SIZE); //找到对应要插入块
				file.writeInt(1); //写入块号
				file.writeInt(LEAF_NODE_TYPE);//写入节点类型
				file.writeInt(1); //写入键数
				file.write(field_value.getBytes()); //写入key0
				file.writeInt(block_id); //写入叶节点的ptr =块号，偏移
				file.writeInt(offset);
				file.seek(BLOCK_SIZE*1+BLOCK_SIZE-4);
				file.writeInt(SPECIAL_INDEX_BLOCK_PTR);
				List<String> key_list=new ArrayList<>();
				List<List<Integer>> ptr_list=new ArrayList<>();
				List<Integer> temp=new ArrayList<>();
				temp.add(block_id);
				temp.add(offset);
				key_list.add(field_value); //向key_list插入一条索引记录
				ptr_list.add(temp);
				List<Integer> t=new ArrayList<>();
				t.add(SPECIAL_INDEX_BLOCK_PTR);
				t.add(0);
				ptr_list.add(t);
				this.key_dic.put(1, key_list);
				this.ptr_dic.put(1, ptr_list);
				
				//下面是准备元块节点，存在块0里
				file.seek(0);
				file.writeInt(0); //写入块号
				file.writeBoolean(true); //写入是否有根
				file.writeInt(1); //写入层数
				file.writeInt(1); //写入根节点指针（块号）
				file.writeInt(2);
				
				this.has_root=true;
				this.num_of_levels=1;
				this.root_node_ptr=1;
				this.num_of_blocks=2; //当前块数
				file.close();
				System.out.println("已经插入索引成功！");
			}
			else{ //有信息在索引文件里,那么从根开始，一直往下找到应该插入的叶节点
				System.out.println("该索引文件有内容");
				file=new RandomAccessFile(this.path+this.filename+".ind","rw");
				file.seek(0);
				int temp_blockid=file.readInt();
				this.has_root=file.readBoolean();
				this.num_of_levels=file.readInt();
				this.root_node_ptr=file.readInt();
				this.num_of_blocks=file.readInt();
				System.out.println("块号："+temp_blockid);
				System.out.println("是否有根："+this.has_root);
				System.out.println("层数："+this.num_of_levels);
				System.out.println("块数："+this.num_of_blocks);
				if((this.has_root==true)&&(this.num_of_levels>0)&&(this.root_node_ptr>0)){	
					int temp_count=0;
					List<Integer> inherit_list=new ArrayList<>();
					int next_node_ptr=this.root_node_ptr; //下一个节点为根节点
					inherit_list.add(next_node_ptr); 
					
					while(temp_count<this.num_of_levels-1){//还没有搜索到最后一层,即肯定是内部节点
						System.out.println("现在是在穿过内部节点到达叶节点");
						int read_pos=BLOCK_SIZE*next_node_ptr;
						file.seek(read_pos);
						int current_id=file.readInt();
						int current_node_type=file.readInt();
						int current_num_of_keys=file.readInt();
						if(current_node_type!=INTERNAL_NODE_TYPE){
							System.out.println("内部节点类型错误！");
							return;
						}
						if(current_num_of_keys<=0){
							System.out.println("内部节点的键数错误！");
							return;
						}
						//下面生成内部节点的键值列表和指针列表
						List<String> internal_key_list=new ArrayList<>();
						List<Integer> internal_ptr_list=new ArrayList<>();
						for(int i=0;i<current_num_of_keys;i++){
							byte[] current_key=new byte[10];
							file.read(current_key); //读取键值
							internal_key_list.add(new String(current_key)); //加入内部节点键值列表
							int current_ptr=file.readInt(); //读取指针
							internal_ptr_list.add(current_ptr);
						}
						file.seek(read_pos+BLOCK_SIZE-4); //定位到该块的最后4字节处
						int last_ptr=file.readInt(); //读取最后一个指针
						internal_ptr_list.add(last_ptr);
						
						//因为是内部节点，所以要决定在内部节点沿哪条路径向下走，即要将记录插在叶节点则，则应该在当前内部节点继续向下去到哪一个节点？
						next_node_ptr=get_next_block_ptr(field_value,internal_key_list,internal_ptr_list);
						if(next_node_ptr==-1)
						{
							System.out.println("下一个节点错误！");
							return;
						}
						inherit_list.add(next_node_ptr);
						temp_count+=1; //层数加1，表示向下走一层
					}
					//现在处于叶节点
					System.out.println("现在已经到达叶节点");
					int read_pos=BLOCK_SIZE*next_node_ptr;
					file.seek(read_pos);
					int current_id=file.readInt();
					int current_node_type=file.readInt();
					int current_num_of_keys=file.readInt();
					System.out.println("当前块号："+current_id);
					System.out.println("当前节点类型："+current_node_type);
					System.out.println("当前节点键数："+current_num_of_keys);
					file.seek(read_pos+BLOCK_SIZE-4); //指到最后4字节处
					int last_pos=file.readInt();
					if(current_node_type==LEAF_NODE_TYPE){//当前节点是叶子节点
						//下面生成叶节点的键值列表，指针列表，叶节点的指针是两个数字，指向该键值对应的记录所在的数据块的块号和在数据块里的偏移
						List<String> key_list=new ArrayList<>();
						List<List<Integer>> ptr_list=new ArrayList<>();
						for(int i=0;i<current_num_of_keys;i++){
							file.seek(read_pos+12+i*18);
							byte[] current_key=new byte[10];
							file.read(current_key);
							System.out.println("当前键值"+new String(current_key));
							key_list.add(new String(current_key));
							int current_ptr=file.readInt();
							int current_off=file.readInt();
							List<Integer> temp=new ArrayList<>();
							temp.add(current_ptr);
							temp.add(current_off);
							ptr_list.add(temp);
							System.out.println("当前指针"+temp);
						}
						List<Integer> tmp=new ArrayList<>();
						tmp.add(last_pos);
						tmp.add(0);
						ptr_list.add(tmp);
						System.out.println("未插入之前简直列表："+key_list);
						System.out.println("未插入之前指针列表："+ptr_list);
						if(current_num_of_keys<MAX_NUM_OF_KEYS){ //当前这个叶节点的键值数<最大键值数，即还可以继续插入键值
							List<Integer> temp=new ArrayList<>();
							temp.add(block_id); //是要插入键值的ptr
							temp.add(offset);
							insert_key_into_leaf_list(field_value,temp,key_list,ptr_list); //调用插入叶节点函数
							System.out.println("插入以后的键值"+key_list);
							System.out.println("插入以后的指针"+ptr_list);
							this.key_dic.put(next_node_ptr, key_list);
							this.ptr_dic.put(next_node_ptr, ptr_list);
							read_pos=next_node_ptr*BLOCK_SIZE;
							//循环将key_list和ptr_list写入文件
							for(int j=0;j<key_list.size();j++){
								String current_key=key_list.get(j); //获取的键值是10字节的
								System.out.println("xieru wenjian获取的键值为："+current_key);
								List<Integer> tuple=new ArrayList<>();
								tuple=ptr_list.get(j);
								System.out.println("wenjian获取的指针为："+tuple);
								file.seek(read_pos+12+j*LEN_OF_LEAF_NODE);
								file.write(current_key.getBytes());
								file.writeInt(tuple.get(0));
								file.writeInt(tuple.get(1));
							}
							current_num_of_keys+=1; //键数+1
							last_pos=ptr_list.get(ptr_list.size()-1).get(0);
							file.seek(read_pos);
							file.writeInt(current_id);
							file.writeInt(current_node_type);
							file.writeInt(current_num_of_keys);
							file.seek(read_pos+BLOCK_SIZE-4);
							file.writeInt(last_pos);
						}
						else{
							System.out.println("当前叶节点已满，需要分裂");
							this.num_of_blocks+=1;
							List<Integer> temp=new ArrayList<>();
							temp.add(block_id); //是要插入键值的ptr
							temp.add(offset);
							insert_key_into_leaf_list(field_value,temp,key_list,ptr_list); //调用插入叶节点函数
							node_split(key_list,ptr_list,current_id,inherit_list);
							
						}
					}
					else{
						System.out.println("错误！，它应该是一个叶节点！");
					}
				file.close();
				System.out.println("已经插入索引成功！");
				}
				else{
					System.out.println("索引文件的信息错误！");
				}
			}
		}
		
	}
	//节点分裂,current_id指向当前块（要分裂的块）
	public void node_split(List<String> key_list,List<List<Integer>> ptr_list,int current_id,List<Integer> inherit_list) throws IOException{
		System.out.println("inherit_list为："+inherit_list);
		System.out.println("------------------------");
		file=new RandomAccessFile(this.path+this.filename+".ind","rw");
		file.seek(current_id*BLOCK_SIZE);
		current_id=file.readInt(); //读取当前块号
		int current_node_type=file.readInt(); //读取节点类型
		int current_num_of_keys=file.readInt(); //读取键数
		file.seek(current_id*BLOCK_SIZE+BLOCK_SIZE-4);
		int last_ptr=file.readInt(); //读取最后一个指针
		file.close();
		if(current_node_type==LEAF_NODE_TYPE){
			file=new RandomAccessFile(this.path+this.filename+".ind","rw");
			//当前节点是叶节点,分裂后的左边节点key个数=（n+1）/2上取整，右边节点的key个数=（n+1）/2下取整
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
			System.out.println("左边的分裂节点的键值"+left_leaf_key_list);
			System.out.println("右边的分裂节点的键值"+right_leaf_key_list);
			
			file.seek(0);
			int block_id=file.readInt();
			Boolean has_node=file.readBoolean();
			int num_of_level=file.readInt();
			int root_ptr=file.readInt();
			int num_of_block=file.readInt();
			
			int right_id=num_of_block; //原来的块数分配给分裂之后的右节点（比如原来块数为2，块号是0、1，所以新的块号为2）
			num_of_block+=1; //节点分裂以后，会产生一个新的节点，所以块数+1(一个节点对应一个块)
			if(num_of_level==1){ //如果只有一层，则层数一定+1
				num_of_level+=1;
			}
			file.seek(BLOCK_SIZE*right_id); //指向新生成的块
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
			file.writeInt(last_ptr); //右边分裂节点的最后一个指针还是原来的文件的最后一个指针
			List<Integer> temp=new ArrayList<>();
			temp.add(last_ptr);
			temp.add(0);
			right_leaf_ptr_list.add(temp);
			
			key_dic.put(right_id, right_leaf_key_list);
			ptr_dic.put(right_id, right_leaf_ptr_list);
			
			//下面重新写入分裂以后的左节点，它仍然在原来的块里
			file.seek(BLOCK_SIZE*current_id); //指向原来的块
			file.writeInt(current_id);
			file.writeInt(LEAF_NODE_TYPE);
			file.writeInt(num_of_left_leaf); //现在这个块里的键数是左节点的键数
			for(int i=0;i<num_of_left_leaf;i++){
				file.seek(BLOCK_SIZE*current_id+i*18+12);
				file.write(left_leaf_key_list.get(i).getBytes());
				file.writeInt(left_leaf_ptr_list.get(i).get(0));
				file.writeInt(left_leaf_ptr_list.get(i).get(1));
				
			}
			file.seek(BLOCK_SIZE*current_id+BLOCK_SIZE-4);
			file.writeInt(right_id); //新的块号作为旧块的last_ptr(旧块的最后一个指针指向新块)
			List<Integer> tmp=new ArrayList<>();
			tmp.add(right_id);
			tmp.add(0);
			left_leaf_ptr_list.add(tmp);
			key_dic.put(current_id, left_leaf_key_list);
			ptr_dic.put(current_id, left_leaf_ptr_list);
			//下面写入元信息
			file.seek(0);
			file.writeInt(block_id);
			file.writeBoolean(has_node);
			file.writeInt(num_of_level);
			file.writeInt(root_ptr);
			file.writeInt(num_of_block);
			
			if((inherit_list.size()==1)&&(inherit_list.get(0)==current_id)){
				//只有一层节点，即当前还没有内部节点，这个是唯一的叶节点，
				System.out.println("现在执行的是temp1=[]"+inherit_list);
				List<Integer> temp1=new ArrayList<>();
				//使用右边分裂节点的键值的第一个元素作为增加的内部节点的键
				System.out.println("temp1"+temp1);
				insert_internal_node(right_leaf_key_list.get(0),current_id,right_id,temp1);
			}
			else{
				System.out.println("现在执行的是temp=inherit_list[-1]"+inherit_list);
				List<Integer> temp1=new ArrayList<>();
				//temp里面是inherit_list的除去最后一个元素的列表
				for(int j=0;j<inherit_list.size()-1;j++){
					temp1.add(inherit_list.get(j));
				}
				System.out.println("temp1="+temp1);
				insert_internal_node(right_leaf_key_list.get(0),current_id,right_id,temp1);
			}
			file.close();
		}
		else if(current_node_type==INTERNAL_NODE_TYPE){//当前节点是内部节点
			file=new RandomAccessFile(this.path+this.filename+".ind","rw");
			int num_left_block=(int)Math.ceil(((float) MAX_NUM_OF_KEYS+1)/2);
			int num_right_block=(int)Math.floor(((float) MAX_NUM_OF_KEYS+1)/2)-1;
			//划分key_list和ptr_list成为左右两部分，成为左右节点
			List<String> left_block_key_list=new ArrayList<>();
			List<Integer> left_block_ptr_list=new ArrayList<>();
			for(int i=0;i<num_left_block;i++){
				left_block_key_list.add(key_list.get(i));
				left_block_ptr_list.add(ptr_list.get(i).get(0));
			}
			left_block_ptr_list.add(ptr_list.get(num_left_block).get(0));
			//中间的节点不在左节点也不在右节点，而是向上一层传递，其对应的ptr作为左节点的last_ptr
			String mid_key=key_list.get(MAX_NUM_OF_KEYS/2+1);
			int mid_ptr=ptr_list.get(MAX_NUM_OF_KEYS/2+1).get(0);
			System.out.println("中间节点的键值为+"+mid_key);
			System.out.println("中间节点指针为"+mid_key);
			List<String> right_block_key_list=new ArrayList<>();
			List<Integer> right_block_ptr_list=new ArrayList<>();
			for(int i=num_left_block+1;i<MAX_NUM_OF_KEYS+1;i++){
				right_block_key_list.add(key_list.get(i));
				right_block_ptr_list.add(ptr_list.get(i).get(0));
			}
			right_block_ptr_list.add(ptr_list.get(ptr_list.size()-1).get(0));
			System.out.println("分裂左边内部节点键值为+"+left_block_key_list);
			System.out.println("分裂后左边指针列表为+"+left_block_ptr_list);
			System.out.println("分裂右边键值列表为+"+right_block_key_list);
			System.out.println("分裂右边指针列表为+"+right_block_ptr_list);
			file.seek(0);
			int block_id=file.readInt(); 
			Boolean has_node=file.readBoolean();
			int num_of_level=file.readInt();
			int root_ptr=file.readInt();
			int num_of_block=file.readInt();
			
			int right_id=num_of_block;  //右节点的块号=新添加的块号，即最后一个块号等于右节点块号
			num_of_block+=1; //块数+1
			if(inherit_list.size()==1){ //如果父子关系只有一层，则表示已经到达根节点，根节点如果进行分裂，则树要增高一层
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
			file.writeInt(last_ptr); //旧节点的last_ptr作为新节点的last_ptr
			
			
			//重新写入左节点
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
				//只有一层节点，即当前还没有内部节点，这个是唯一的叶节点，
				System.out.println("现在执行的是temp=[] "+inherit_list);
				List<Integer> temp=new ArrayList<>();
				insert_internal_node(mid_key,current_id,right_id,temp);
			}
			else{
				System.out.println("现在执行的是temp=inherit_list[-1]"+inherit_list);
				List<Integer> temp=new ArrayList<>();
				//temp里面是inherit_list的除去最后一个元素的列表
				for(int j=0;j<inherit_list.size()-1;j++){
					temp.add(inherit_list.get(j));
				}
				insert_internal_node(mid_key,current_id,right_id,temp);
			}
		}
		
	}
	//插入一个内部节点
	public void insert_internal_node(String new_key,int left_ptr,int right_ptr,List<Integer> inherit_list) throws IOException{
		System.out.println("现在执行一个插入内部节点函数");
		System.out.println(inherit_list);
		System.out.println("----------------------------");
		file=new RandomAccessFile(this.path+this.filename+".ind","rw");
		file.seek(0);
		int block_id=file.readInt();
		Boolean has_node=file.readBoolean();
		int num_of_level=file.readInt();
		int root_ptr=file.readInt();
		int num_of_block=file.readInt();
		int this_internal_id; //是新添加的内部节点的id
		if(inherit_list.size()==0){//本来没有父子关系，说明这是添加的新的root
			System.out.println("原来没有父子关系，现在新加一个root");
			
			this_internal_id=num_of_block; //新添加的内部节点的块号
			num_of_block+=1;
			System.out.println("当前节点的块号："+this_internal_id);
			root_ptr=this_internal_id; //新添加的块就是根节点
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
			System.out.println("成功插入一个跟");
		}
		else{//原来有父子关系
			System.out.println("原来有父子关系");
			file.seek(inherit_list.get(inherit_list.size()-1)*BLOCK_SIZE); //是最下面一层父亲，
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
					temp.add(0); //是内部节点，内部节点的ptr只有一个block_id,而叶节点的ptr是（block_id,offset）,默认内部节点的offset为0
					ptr_list.add(temp);
				}
				
				//将新键值new_key插入节点里的键值列表里，插入到合适的位置
				if(new_key.compareTo(key_list.get(key_list.size()-1))>0){ //当前插入的键值大于当前节点里的所有键值
					List<Integer>temp=new ArrayList<>();
					temp.add(last_ptr);
					temp.add(0);
					ptr_list.add(temp); //原来的最后一个指针被现在新的键值的右指针取代
					last_ptr=right_ptr;
					key_list.add(new_key);
				}
				else{
					int pos=-1;
					for(int j=0;j<key_list.size();j++){
						//遍历key_list的所有键值，确定new_key应该插在哪里
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
				System.out.println("当前键值列表"+key_list);
				System.out.println("当前ptr列表："+ptr_list);
				current_num_of_keys+=1;
				if(current_num_of_keys<=MAX_NUM_OF_KEYS){ //当前键数没有到达最大，不需要分裂
					file.seek(current_id*BLOCK_SIZE); //定位到当前节点
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
		System.out.println("当前块号："+block_id);
		System.out.println("是否有根："+has_root);
		System.out.println("层数："+levels);
		System.out.println("根节点指针："+rootptr);
		System.out.println("块数："+blocksnum);
		for(int i=1;i<blocksnum;i++){
			file.seek(i*BLOCK_SIZE);
			int id=file.readInt();
			int type=file.readInt();
			int keysnum=file.readInt();
			System.out.println("块号："+id);
			System.out.println("节点类型："+type);
			System.out.println("键数："+keysnum);
			if(type==0){//内部节点
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
				System.out.println("内部节点键值列表："+key_list);
				System.out.println("内部节点指针列表："+ptr_list);
			}
			else if(type==1){//是叶节点
				
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
				System.out.println("叶节点键值列表："+key_list);
				System.out.println("叶节点指针列表："+ptr_list);
			}
			
		}
	}
}




