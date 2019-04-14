package xmj.minibase01;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.StandardSocketOptions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import xmj.minibase01.Schema;

public class Storage {
	public static int BLOCK_SIZE = 4096;
	public List<List<String>> record_list=new ArrayList<>(); //记录列表
	public List<List<Integer>> record_position=new ArrayList<>(); //记录位置列表
	public List<List<String>> field_name_list=new ArrayList<>();  //域名列表
	public String path="D:\\minibase_data\\table\\";
	public String tableName=null;
	public RandomAccessFile file=null;
	public boolean open;
	public int num_of_fields;
	public Scanner sc=new Scanner(System.in);
	public int block_id=0; //块id
	public int data_block_num=0; //数据块的个数
	public int num_of_records=0;
	public int recordheadlen=18;
	public int record_len=0;
	
	public  String fillTablename(String tableName){
		if(tableName.trim().length()<10){
			tableName = tableName.format("%10s", tableName);// 表名格式化
		}
		return tableName;
	}
	
	public List<List<String>> get_fieldlist(){
		return this.field_name_list;
	}
	
	public boolean deletetabledata(String tablename){
		if(this.open==true){
			this.open=false;
		}
		this.tableName=tablename.trim();
		File f=new File(this.path+this.tableName+".dat");
		if(f.exists()){
			f.delete();
			System.out.println(tablename.trim()+"表删除成功！");
			return true;
		}
		return false;
	}
	public Storage(String tablename) throws IOException{
		this.tableName=tablename.trim();
		File f=new File(this.path+this.tableName+".dat");
		if(!f.exists()){
			System.out.println("文件"+this.tableName+".dat不存在");
			//文件不存在，则创建一个文件
			f.createNewFile();
			this.open=false;
			System.out.println("文件"+this.tableName+".dat已经被创建");			
		}
		file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
		System.out.println("打开文件"+this.tableName+".dat");
		this.open=true;
		byte[] dir_buf=new byte[BLOCK_SIZE];
		file.seek(0);
		int mylen=file.read(dir_buf); //从表文件里读取BLOCK_SIZE大小的内容
		file.close();
		int begin_index=0;
		
		if(mylen==-1) //数据文件里没有内容，则新建内容
		{
			this.data_block_num+=1;
			System.out.println("请输入域个数：");
			this.num_of_fields=sc.nextInt();
			if(this.num_of_fields>0)//如果域个数大于0
			{
				file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
				file.seek(0);
				file.writeInt(0); //写入block_id
				file.writeInt(this.data_block_num);
				file.writeInt(this.num_of_fields);
				
				begin_index+=12; //头部三个字段占了12字节
				
				for (int i = 0; i < this.num_of_fields; i++) {
					System.out.println("请输入域名：");
					String field_name = sc.next();
					if (field_name.length() < 10) {
						field_name = fillTablename(field_name);
					}
					int field_type;
					while (true) {
						System.out.println("请输入域类型：（0代表str，1代表int，2代表booLean）");
						field_type = sc.nextInt();
						if (field_type == 0 || field_type == 1 || field_type == 2)
							break;
						System.out.println("请重新输入!");
					}
					System.out.println("请输入域长度：");
					int fieldlen = sc.nextInt();
					List<String> temp_list = new ArrayList<>();
					temp_list.add(field_name);
					temp_list.add(Integer.toString(field_type));
					temp_list.add(String.valueOf(fieldlen));
					this.field_name_list.add(temp_list); // 将[域名，域类型，域长度]列表加入field_name_list

					// 将域信息写入数据文件
					
					file.seek(begin_index);
					file.write(field_name.getBytes());
					file.writeInt(field_type);
					file.writeInt(fieldlen);
					begin_index += Schema.MAX_FIELD_LEN;

				}
				
			}
			file.close();
		}
		else //数据文件里有内容
		{
			file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
			file.seek(0);
			this.block_id=file.readInt();
			this.data_block_num=file.readInt();
			this.num_of_fields=file.readInt();
			System.out.println("当前数据块为："+this.block_id+",数据块个数为："+this.data_block_num+",域个数为："+this.num_of_fields);
			begin_index+=12;
			for(int i=0;i<this.num_of_fields;i++){
				file.seek(begin_index);
				byte[] name=new byte[10];
				file.read(name); //读取10个字节的内容
				String field_name=new String(name); //根据字节数组创建一个字符串
				int field_type=file.readInt();
				int field_len=file.readInt();
				List<String> temp_list=new ArrayList<>();
				temp_list.add(field_name);
				temp_list.add(Integer.toString(field_type));
				temp_list.add(String.valueOf(field_len));
				this.field_name_list.add(temp_list); //将域信息加入field_name_list
				System.out.println("第"+(i+1)+"域的信息如下：域名:"+field_name+"，域类型:"+field_type+"，域长度"+field_len);
				begin_index+=18;
			}
			file.close();
		}
		
		//下面代码生成record_List,和record_position的
		Stream<List<String>> st=this.field_name_list.stream();
		//获取每个域的长度，组成一个列表
		List<String> fieldlen_list=st.map((x)->x.get(2)).collect(Collectors.toList());
		int record_content_len=0; //这是每个记录的长度，每个域的长度之和
		for(int i=0;i<fieldlen_list.size();i++){
			record_content_len+=Integer.parseInt(fieldlen_list.get(i));
		}
		
		int flag=1;
		while(flag<this.data_block_num) //遍历所有的数据块
		{
			begin_index=BLOCK_SIZE*flag;  //起始索引指向第一个数据块的开头（block_0）
			file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
			file.seek(begin_index);
			this.block_id=file.readInt(); //读取块id
			this.num_of_records=file.readInt();  //读取记录数
			System.out.println("当前块ID="+this.block_id+",有"+this.num_of_records+"条记录");
			//如果有记录存在，就将记录加入到record_list，record_position里去
			if(this.num_of_records>0){
				for(int i=0;i<this.num_of_records;i++){
					List<Integer> temp_pos=new ArrayList<>();
					temp_pos.add(flag);
					temp_pos.add(i);
					this.record_position.add(temp_pos);
					
					file.seek(begin_index+8+i*4);  //找到“记录i的偏移”所在位置
					int offset=file.readInt(); //读记录i的偏移
					int record_des=offset+this.recordheadlen;  //这是该记录在数据文件的位置，记录i的偏移（在本块内的相对位移）+begin_index（这是在各个块的起始地址）+记录的头长度
					file.seek(record_des+begin_index); //找到记录i
					//下面就是生成record_list
					List<String> temp_list=new ArrayList<>();  //是记录列表，列表里的每个元素是各个域的取值
					for(int j=0;j<this.num_of_fields;j++){
						byte[] attr=new byte[Integer.parseInt(this.field_name_list.get(j).get(2))];
						file.read(attr); //去读字节数组
						String attr1=new String(attr); //attr1里面就是第j个域所取得值
						temp_list.add(attr1); 
					}
					this.record_list.add(temp_list);
				}
				
			}
			flag+=1;
			file.close();
		}
		
		
	}
	//获取所有的记录
	public List<List<String>> getrecord(){
		return this.record_list;
	}
	
	//插入一条记录
	public boolean insert_record(List<String> insertrecord) throws IOException{
		List<String> temprecord=new ArrayList<>(); 
		String pk=insertrecord.get(0);
		int ispk=-1;
		for(int i=0;i<this.record_list.size();i++){
			if(pk.equals(this.record_list.get(i).get(0).trim())){
				System.out.println("插入记录的主键值在表中已经存在，不可插入！");
				ispk=i;
				break;
			}
		}
		if(ispk<0){// 不存在这个主键，可以插入这个记录
			for(int i=0;i<this.num_of_fields;i++){
				insertrecord.set(i, insertrecord.get(i).trim()); //将插入记录的域取值去除空格
				if(Integer.parseInt(this.field_name_list.get(i).get(1))==0) //如果该域类型为str或者varstr
				{
					if(insertrecord.get(i).length()>Integer.parseInt(this.field_name_list.get(i).get(2))) //如果插入记录在该域上的长度超过了规定的域长度
					{
						return false;
					}
					temprecord.add(insertrecord.get(i)); //符合长度要求，则加入临时记录列表
				}
				if(Integer.parseInt(this.field_name_list.get(i).get(1))==1)//是int类型
				{
					try{
						temprecord.add(insertrecord.get(i));	
					}
					catch (Exception e) {
						return false;
					}	
				}
				if(Integer.parseInt(this.field_name_list.get(i).get(1))==2) //是boolean类型
				{
					try{
						temprecord.add(insertrecord.get(i));	
					}
					catch (Exception e) {
						return false;
					}	
				}
				//下面是在域取值的长度不足域规定的长度时补全域（前面添加上空格）
				int fill_length=Integer.parseInt(this.field_name_list.get(i).get(2))-temprecord.get(i).length(); //得到长度差值
				if(fill_length<0){ //长度小于0，即域取值超过了该域的规定长度
					return false;
				}
				String strb=""; //字符串拼接时建议使用stringbuffer
				for(int j=0;j<fill_length;j++){
					strb+=" ";
				}
				//将insertrecord里的每个域取值补全到域的最大长度
				temprecord.set(i, strb+temprecord.get(i).trim());
				
			}//至此，temprecord里面是符合长度要求的域取值
			
			System.out.println(temprecord);
			StringBuffer recordstr=new StringBuffer(); //获取插入记录里的每个域取值，成为字符串
			for(int j=0;j<this.num_of_fields;j++){
				recordstr.append(temprecord.get(j)); //recordstr是字符串，将域取值列表转换成字符串
			}
			System.out.println("当前要插入的记录为："+ new String(recordstr));
			this.record_list.add(temprecord); //将插入记录加入到record_list里去
			System.out.println("当前记录列表："+this.record_list);
			//下面是修改record_position ，添加一个元素（是新记录的位置）
			//下面计算数据块里最大记录数
			int recordcontentlen=recordstr.toString().length(); //记录体的长度
			this.record_len=recordcontentlen+this.recordheadlen; //记录的长度（包括记录头和记录体）
			int MAX_RECORD_NUM=(BLOCK_SIZE-8)/(4+this.record_len); //-8是去除一个块前面的block id和记录数两个字段，4+record_len是每个记录的偏移和记录长度
			List<Integer> temp=new ArrayList<>();
			//下面计算新的记录的位置
			if(this.record_position.size()==0){ //原来没有记录存在
				System.out.println("原来没有数据存在");
				this.data_block_num+=1; //数据块数+1
				temp=new ArrayList<>();
				temp.add(1);
				temp.add(0);
				this.record_position.add(temp); //将[1,0]加入record_position
				
			}
			else{  //原来有记录存在
				System.out.println("原来有数据存在");
				List<Integer> last_pos=new ArrayList<>();
				last_pos=this.record_position.get(this.record_position.size()-1); //获取最后一条记录的位置
				if(last_pos.get(1)==MAX_RECORD_NUM-1) 
				//上一条记录已经存在了这一个数据块的最后一个位置上，即现在这个数据块已经不可以再存放数据了
				{
					System.out.println("这块数据块已经存满，另开辟一块数据块");
					temp=new ArrayList<>();
					temp.add(last_pos.get(0)+1); //存放的数据块id+1
					temp.add(0);
					this.record_position.add(temp); //新位置加入record_position
					this.data_block_num+=1;  //新使用了一个数据块，所以数据块个数+1
				}
				else  //这个数据块不满，仍可以插入记录
				{
					System.out.println("这块数据块还有空闲");
					temp=new ArrayList<>();
					temp.add(last_pos.get(0));
					temp.add(last_pos.get(1)+1);
					this.record_position.add(temp);
				}
				
			}
			List<Integer> last_pos=new ArrayList<>();
			System.out.println("现在数据文件里有"+this.record_position);
			last_pos=this.record_position.get(this.record_position.size()-1);
			System.out.println("最后一条记录的位置："+last_pos);
			//下面是向xxx.dat文件添加插入的记录
			//更新data_block_num（更新文件开头 的block0，和data_block_num）
			file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
			file.seek(0);
			file.writeInt(0);
			file.writeInt(this.data_block_num);
			file.writeInt(this.num_of_fields);
			//更新记录数num_of_records
			file.seek(BLOCK_SIZE*last_pos.get(0)); //定位到最后一个记录的块开始处
			file.writeInt(last_pos.get(0)); //块id保持不变
			file.writeInt(last_pos.get(1)+1); //更新记录数
			
			
			//更新offset，插入记录偏移
			int offset=BLOCK_SIZE*last_pos.get(0)+8+4*last_pos.get(1); //指向最后一个记录位置（这个位置是最后一个记录的偏移）
			int begin_index=BLOCK_SIZE-this.record_len*(last_pos.get(1)+1); //指向最后一条记录真实存在的位置不是记录的偏移存的位置
			file.seek(offset); //找到记录偏移处
			file.writeInt(begin_index); //将记录的偏移写入文件
			
			//更新数据，插入记录
			Date updatetime=new Date(); //获取当前时间
			SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-mm-dd"); //设置时间格式
			String strdate=dateformat.format(updatetime); //转换成字符串
			file.seek(BLOCK_SIZE*last_pos.get(0)+begin_index+4); //指向我记录实际要插入的位置，+4是记录头部的pointer（占4字节？？不是12字节么）
			file.writeInt(this.record_len); //写入记录的长度
			file.write(strdate.getBytes()); //写入更新的时间
			file.write(recordstr.toString().getBytes()); //写入记录主体
			file.close();
			return true;
			
		}
		return false;
		
	}
	
	//删除一条记录
	public boolean delete_record(String primarkeyvalue) throws IOException {
		primarkeyvalue=primarkeyvalue.trim();
		int pos = -1;
		int record_offset = 0; // 该记录存储位置
		int blockid = 0;
		int recordsnum = 0;
		int recordpos = 0;
		int recordlen=0;
		for(int i=0;i<this.field_name_list.size();i++){
			recordlen+=Integer.parseInt(this.field_name_list.get(i).get(2));
		}
		List<Integer> temp = new ArrayList<>();
		for (int i = 0; i < this.record_list.size(); i++)// 对所有的记录，查看包含主键值的记录是否存在
		{
			if (primarkeyvalue.equals(this.record_list.get(i).get(0).trim())) {
				pos = i; // 找到该条记录，记下它的位置i
			}
		}
		if (pos == -1) {
			System.out.println("该条记录不存在！");
			return false;
		} else {
			
			System.out.println("要删除的记录为"+this.record_list.get(pos));
			this.record_list.remove(pos); // 删除这个下标出的记录
			
			temp = this.record_position.get(pos); // 获取这条记录的位置[block id,offset]
			blockid = temp.get(0);
			recordpos = temp.get(1);
			System.out.println("要删除的记录在第" + blockid + "块，要删除的记录是第" + (recordpos+1) + "条记录。");
			this.record_position.remove(pos); // 删除这个下标处的记录位置
			
			file = new RandomAccessFile(this.path + this.tableName + ".dat", "rw");
			file.seek(BLOCK_SIZE * blockid + 4); // 找到这个记录所在块的记录数
			recordsnum = file.readInt(); // 读取该块的记录数
			file.close();

			if (recordsnum == 1) { // 如果只有要删除的这一条记录存在，则删除这个块
				
				this.data_block_num -= 1; // 块数-1
				file = new RandomAccessFile(this.path + this.tableName + ".dat", "rw");
				file.seek(0);
				file.writeInt(0);
				file.writeInt(this.data_block_num);
				this.num_of_records -= 1; // 记录数-1
				file.close();
			} else {
				this.num_of_records -= 1; // 记录数-1
				
			}
			file = new RandomAccessFile(this.path + this.tableName + ".dat", "rw");
			file.seek(BLOCK_SIZE * blockid );
			file.writeInt(block_id);
			file.writeInt(this.num_of_records);
			file.close();
			file=new RandomAccessFile(this.path + this.tableName + ".dat", "rw");
			
			for(int p=recordpos+1;p<this.num_of_records+1;p++){
				int off=BLOCK_SIZE*blockid+8+p*4;
				file.seek(off);
				int offset1=file.readInt(); //读取移动记录的偏移地址
				file.seek(BLOCK_SIZE*blockid+offset1+this.recordheadlen);
				byte[] record=new byte[recordlen];
				file.read(record);
				System.out.println("当前移动的记录为"+new String(record));
				int off1=BLOCK_SIZE*blockid+8+(p-1)*4;
				file.seek(off1);
				int offset2=file.readInt(); //读取到前一个记录的偏移
				file.seek(BLOCK_SIZE*blockid+offset2+this.recordheadlen);
				file.write(new String(record).getBytes());
			}
			file.close();
			System.out.println("记录删除成功！");
			return true;
			
			//要删除的记录后面的记录都要移动
			/*for (int p = recordpos; p < this.num_of_records; p++) 
			{//record_offset指向要后面记录要插入的位置
				record_offset = BLOCK_SIZE - (p + 1) * this.record_len;  //指向要删除的记录的位置
				file.seek(BLOCK_SIZE*blockid+record_offset-this.record_len+this.recordheadlen); //指向要删除的记录的前一条记录位置
				byte[] fields=new byte[recordlen]; //一个记录总长度的数组
				file.read(fields);
				System.out.println("现在要移动的记录："+new String(fields));
				file.seek(BLOCK_SIZE*blockid+record_offset+this.recordheadlen);
				file.write(new String(fields).getBytes());
				*/
				
				//找到后面一条记录的记录体所在的位置，表达式后面+ this.recordheadlen是为了跨过记录头部
				/*int field_len=0;
				for (int k = 0; k < this.field_name_list.size(); k++) {
					file.seek(BLOCK_SIZE * blockid + record_offset - this.record_len + this.recordheadlen); // 找到要删除的记录的下一条记录的体的位置
					
					//byte数组是根据每个域的长度生成的
					byte[] fieldvalue = new byte[Integer.parseInt(field_name_list.get(k).get(2))]; // 生成第k个域对应长度的字节数组
					file.read(fieldvalue); // 读取域取值
					
					file.seek(BLOCK_SIZE * blockid + record_offset + this.recordheadlen+field_len); // 指向后面记录要插入的位置
					file.write(new String(fieldvalue).getBytes());
					field_len+=Integer.parseInt(field_name_list.get(k).get(2)); //field_len存的域长度累加
				}*/
			//}
			/*file.close();
			System.out.println("记录删除成功！");
			return true;*/
		}
	}
	
	//更新记录
	public boolean update_record(String primarykeyvalue) throws IOException{
		primarykeyvalue=primarykeyvalue.trim();
		int pos=-1; //pos指向这条记录的位置
		String newfield=new String();
		String newvalue="";
		int blockid=0;
		int record_off=0;
		for(int i=0;i<this.record_list.size();i++){
			if(primarykeyvalue.equals(this.record_list.get(i).get(0).trim())){
				pos=i;
			}
		}
		if(pos==-1){
			System.out.println("不存在这条记录！");
			return false;
		}
		else{
			//输出域名
			System.out.println("该表的域名有：");
			for(int i=0;i<this.field_name_list.size();i++){
				System.out.print(i+":"+this.field_name_list.get(i).get(0)+"\t");
			}
			int up_index=-1; //up_index指向要修改的是第几个域
			while(true){ //确保输入的索引是符合要求的（在域个数范围内）
				System.out.println("请输入需要改变的域的索引：(0-"+(this.field_name_list.size()-1)+")");
				up_index=sc.nextInt();
				//检查输入的up_index是否合法
				for(int k=0;k<this.field_name_list.size();k++){
					if(up_index==k)
						break;
				}
				if(up_index!=-1)
					break;
			}
			
			int field_type=Integer.parseInt(this.field_name_list.get(up_index).get(1)); //获取要修改的域的类型
			int field_len=Integer.parseInt(this.field_name_list.get(up_index).get(2));  //获取要修改的域的长度
			System.out.println("该域的类型为："+field_type+"该域的长度为："+field_len);
			
			//下面确保输入的域值长度合法
			while(true){
				System.out.println("请输入该域修改后的值：");
				newvalue=sc.next();
				if(newvalue.length()>field_len){
					System.out.println("输入的域取值超过规定长度，错误！");
				}
				else
					break;
			}
			//下面填充域取值，使得其为规定长度
			String space="";
			for(int t=0;t<field_len-newvalue.length();t++){
				space+=" ";
			}
			newvalue=space+newvalue;
			List<String> record=this.record_list.get(pos); //获取要修改的记录
			System.out.println("修改前的记录为："+record);
			record.set(up_index, newvalue); //修改记录这个域处的值
			System.out.println("修改以后的记录为："+record);
			this.record_list.set(pos,record);
			int totallen=0; //前面没有改动的域的长度之和
			for(int i=0;i<up_index;i++){
				totallen+=Integer.parseInt(this.field_name_list.get(i).get(2));
			}
			//下面修改文件里的记录的内容
			List<Integer> temp=new ArrayList<>();
			temp=this.record_position.get(pos); //获取要修改的记录的位置
			blockid=temp.get(0);  //获取这条记录的块id 
			record_off=temp.get(1); //获取这条记录在这个块里的偏移
			System.out.println("要修改的记录位于"+blockid+"块，是第"+record_off+"个记录。");
			file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
			
			file.seek(BLOCK_SIZE*blockid+8+record_off*4);
			int off=file.readInt();
			file.seek(BLOCK_SIZE*block_id+off+this.recordheadlen+totallen);
			file.write(newvalue.getBytes());
			
			/*file.seek(BLOCK_SIZE*blockid+BLOCK_SIZE-(pos+1)*this.record_len+this.recordheadlen+totallen); //指向要修改的域位置
			file.write(newvalue.getBytes()); //将新的域取值写入文件
*/			file.close();
			System.out.println("记录修改成功！");
			return true;
		}
	}
	
	
	//展示表的数据（记录）
	public void show_tabledata(){
		List<String> fieldname=new ArrayList<>(); //保存域名
		for(int i=0;i<this.field_name_list.size();i++){
			fieldname.add(this.field_name_list.get(i).get(0).trim());
		}
		System.out.println("该表的数据如下：");
		for(int i=0;i<fieldname.size();i++){
			if(i!=fieldname.size()-1){ //没有到最后一个域
				System.out.print(fieldname.get(i));
				System.out.print("\t|\t");
			}
			else
				System.out.println(fieldname.get(i));
		}
		for(int i=0;i<this.record_list.size();i++){
			for(int j=0;j<this.record_list.get(i).size();j++){
				if(j!=fieldname.size()-1){
					System.out.print(this.record_list.get(i).get(j).trim());
					System.out.print("\t|\t");
				}
				else{
					System.out.println(this.record_list.get(i).get(j).trim());
				}
			}
		}
		/*for(int i=0;i<this.record_list.size();i++){
			System.out.println(this.record_list.get(i));
		}*/
	}
	
	/*//删除该表文件
	public boolean delete_tabledata(String tablename){
		if(this.open==true) //如果这个文件被打开了
		{
			this.open=false;
		}
		tablename=tablename.trim();
		File f=new File(this.path+tablename+".dat");
		if(f.exists()){
			//如果该文件存在
			f.delete(); //删除该文件
		}
		return true;
	}*/
	//获取域信息列表
	public List<List<String>> getFieldlist(){
		return this.field_name_list;
	}
		
}

