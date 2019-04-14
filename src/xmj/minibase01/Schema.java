package xmj.minibase01;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Schema {
	public static final int META_HEAD_SIZE=12; //元信息头部长度为12
	public static final int MAX_TABLE_NAME_LEN=10; //表名的最大长度为10
	public static final int MAX_TABLE_NUM=100; //最大的表个数
	public static final int TABLE_NAME_ENTRY_LEN=MAX_TABLE_NAME_LEN+4+4; //一个表名入口的长度
	public static final int TABLE_NAME_HEAD_SIZE=MAX_TABLE_NUM*TABLE_NAME_ENTRY_LEN; //表头部长度
	public static final int MAX_FIELD_NAME_LEN=10; //最大域名长度
	public static final int MAX_FIELD_LEN=10+4+4; //域长度
	public static final int MAX_NUM_OF_FIELD_PER_TABLE=5; //每张表最大的域个数
	public static final int FIELD_ENTRY_SIZE_PER_TABLE=MAX_FIELD_LEN * MAX_NUM_OF_FIELD_PER_TABLE;  //每张表总的域的长度
	public static final int MAX_FIELD_SECTION_SIZE = FIELD_ENTRY_SIZE_PER_TABLE * MAX_TABLE_NUM; //总的域部分的大小（所有的表的域长度之和）
	public static final int BODY_BEGIN_INDEX = META_HEAD_SIZE + TABLE_NAME_HEAD_SIZE;
	public static final int count=0; //记录实例个数
	public static Head headObj=null;
	public static final String filename="D:\\minibase_data\\all.sch";
	private int body_begin_index = BODY_BEGIN_INDEX;
	
	//将表名格式化
	public  String fillTablename(String tableName){
		if(tableName.trim().length()<MAX_TABLE_NAME_LEN){
			tableName = tableName.format("%10s", tableName);// 表名格式化
		}
		return tableName;
	}
	//返回有多少实例
	public int howmany(){
		return count;
	}
	//列出所有表的表名
	public void viewTableNames(){
		System.out.println("开始查看所有表名");
		for (int i=0;i<headObj.tableNames.size();i++){
			System.out.println("表名是："+headObj.tableNames.get(i).get(0));
		
		}
		System.out.println("执行完毕！");
	}
	//查看表结构
	public void viewTableStructure(String tablename){
		tablename=tablename.trim();
		System.out.println(tablename+"表的结构如下：");
		List<List<String>> fieldlist=new ArrayList<>();
		for(int i=0;i<headObj.tableNames.size();i++){
			if(headObj.tableNames.get(i).get(0).trim().equals(tablename)){
				fieldlist=headObj.tableFields.get(tablename);
				System.out.println(fieldlist);
			}
		}
	}
	public Schema() throws IOException{
		//System.out.println("初始化schema对象");
		File fi=new File(filename);
		if(!fi.exists()) {
			fi.createNewFile(); //模式文件不存在，则创建一个模式文件
		}
		
		System.out.println("模式文件是："+filename);
		RandomAccessFile file=new RandomAccessFile(fi,"rw");
		int buflen=META_HEAD_SIZE+TABLE_NAME_HEAD_SIZE+MAX_FIELD_SECTION_SIZE; //是整个模式文件的大小(最大的大小)
		byte[] buf=new byte[buflen];
		int len=file.read(buf);
		file.close();
		if(len==-1){ //模式文件里没有内容
			this.body_begin_index=BODY_BEGIN_INDEX; //文件体的起始位置
			file=new RandomAccessFile(filename,"rw");
			file.seek(0); 
			//下面写入metahead信息
			file.writeBoolean(false);
			file.writeInt(0);
			file.writeInt(this.body_begin_index);
			file.close(); 
			
			List<List<String>> tablenamelist=new ArrayList<>();
			Map<String, List<List<String>>> fieldnamelist=new HashMap();
			headObj=new Head(false,0,this.body_begin_index,tablenamelist,fieldnamelist); //创建头部对象
			System.out.println("all.sch里的元信息已经修改以及头部对象已经创建");
			
		}
		else{ //原来模式文件里有内容
			System.out.println("模式文件里有内容");
			file=new RandomAccessFile(filename,"rw");
			file.seek(0);
			Boolean isStored=file.readBoolean();
			int tmptablenum=file.readInt();
			int tmpoffset=file.readInt();
			file.close();
			System.out.println("是否有表存储？"+isStored);
			System.out.println("有几张表存储？"+tmptablenum);
			System.out.println("文件体的偏移？"+tmpoffset);
			
			this.body_begin_index=tmpoffset;
			List<List<String>> namelist=new ArrayList<>();
			Map<String, List<List<String>>> fieldlist=new HashMap();
			
			if(isStored==false){ //模式文件里还没有存储表
				headObj=new Head(false,0,this.body_begin_index,namelist,fieldlist);
				System.out.println("模式文件里没有表");
			}
			else{ //模式文件里有表
				System.out.println("模式文件里有"+tmptablenum+"张表");
				for(int i=0;i<tmptablenum;i++){
					file=new RandomAccessFile(filename,"rw");
					file.seek(META_HEAD_SIZE+i*TABLE_NAME_ENTRY_LEN); //指向模式文件头里该表的位置
					byte[] tmpname=new byte[10];
					file.read(tmpname); //读取表名
					int tmpnum=file.readInt(); //读取域个数
					int temppos=file.readInt();// 读取偏移
					System.out.println("表名："+new String(tmpname).trim());
					System.out.println("表的域个数："+tmpnum);
					System.out.println("表在模式文件体里的位置："+temppos);
					List<String> tempnamemix=new ArrayList<>();
					tempnamemix.add(new String(tmpname));
					tempnamemix.add(String.valueOf(tmpnum));
					tempnamemix.add(String.valueOf(temppos));  //这些值用来生成namelist
					namelist.add(tempnamemix);
					
					
					if(tmpnum>0)//域个数大于0，即有多个域，则生成fieldlist
					{
						List<List<String>> fields=new ArrayList<>();
						for(int j=0;j<tmpnum;j++){
							//file=new RandomAccessFile(filename,"rw");
							file.seek(META_HEAD_SIZE+TABLE_NAME_HEAD_SIZE+j*MAX_FIELD_LEN);
							byte[] fieldname=new byte[10];
							file.read(fieldname); //读取域名
							int tmptype=file.readInt(); //读取域类型
							int tempoff=file.readInt();// 读取域长度
							
							System.out.println("域名"+new String(fieldname).trim()+"\t域类型"+tmptype+"\t域长度"+tempoff);
							List<String> fieldimf=new ArrayList<>();
							fieldimf.add(new String(fieldname));
							fieldimf.add(String.valueOf(tmptype));
							fieldimf.add(String.valueOf(tempoff));  //这些值用来生成这个表对应的域信息列表
							fields.add(fieldimf);
						}
						fieldlist.put(new String(tmpname).trim(), fields); //向fieldslist里添加数据
						file.close();
					}
					
				}
				headObj=new Head(true,tmptablenum,tmpoffset,namelist,fieldlist);
				
			}
		}
		
	}
	 //删除所有的模式文件内容
	public void deleteAll() throws IOException{
		System.out.println("开始执行删除模式文件");
		headObj.tableNames.clear();
		headObj.tableFields.clear();
		headObj.isStored=false;
		headObj.lenOfTableNum=0;
		headObj.offsetOfBody=this.body_begin_index;
		
		File f=new File(filename);
		FileWriter fw=new FileWriter(f);
		fw.write("");
		fw.flush();
		fw.close();
		
	}
	//向模式文件添加一张表
	public void appendtable(String tablename,List<List<String>> fieldlist) throws IOException{
		System.out.println("开始执行插入一张表操作");
		tablename=tablename.trim();
		RandomAccessFile  file=null;
		if(tablename.length()==0||tablename.length()>10||fieldlist.size()==0){
			System.out.println("表名是无效的或者域列表是无效的");
		}
		else{
			int fieldnum=fieldlist.size();  //获取域个数
			System.out.println("下面是向模式文件体里写入域信息");
			/*byte[] fieldbuf=new byte[MAX_FIELD_LEN*fieldnum];
			int beginindex=0;*/
			//对于每个域
			
			//将添加的表的域信息写入文件体
			file=new RandomAccessFile(filename,"rw");
			System.out.println("向模式文件体添加表的域信息");
			for(int i=0;i<fieldnum;i++){
				
				String fieldname=fieldlist.get(i).get(0); //获取域列表的第i个域的域名
				int fieldtype=Integer.parseInt(fieldlist.get(i).get(1)); //获取域列表的第i个域信息的第1个（域类型）
				int fieldlen=Integer.parseInt(fieldlist.get(i).get(2)); //获取域列表的第i个域信息的2个（域长度）
				fieldname=fillTablename(fieldname);
				file.seek(headObj.offsetOfBody+i*MAX_FIELD_LEN);
				file.write(fieldname.getBytes()); //将域的域名、域类型、域长度写入文件
				file.writeInt(fieldtype);
				file.writeInt(fieldlen);
				
			}
			file.close();
			
			System.out.println("修改主存里的头部结构");
			file=new RandomAccessFile(filename,"rw");
			tablename=fillTablename(tablename);
			List<String> namecontent=new ArrayList<>();
			namecontent.add(tablename);
			namecontent.add(String.valueOf(fieldnum));
			namecontent.add(String.valueOf(headObj.offsetOfBody));
			file.seek(META_HEAD_SIZE+headObj.lenOfTableNum*TABLE_NAME_ENTRY_LEN);
			file.write(tablename.getBytes());
			file.writeInt(fieldnum);
			file.writeInt(headObj.offsetOfBody); //词表的偏移地址是原来的偏移地址而不是修改后的body_begin_index
			headObj.isStored=true;
			headObj.lenOfTableNum+=1;
			headObj.offsetOfBody+=fieldnum*MAX_FIELD_LEN;
			headObj.tableNames.add(namecontent);
			headObj.tableFields.put(tablename.trim(),fieldlist);
			file.seek(0);
			file.writeBoolean(headObj.isStored); //写入true
			file.writeInt(headObj.lenOfTableNum);  //写入表数
			file.writeInt(headObj.offsetOfBody); //写入修改后的偏移（下一次新表该插入的位置）
			file.close();
			
			
			
			
		}
	}
	//是否找到指定名称的表
	public boolean find_table(String tablename){
		for(int i =0;i<headObj.tableNames.size();i++){
			if(tablename.equals(headObj.tableNames.get(i).get(0).trim())){
				return true;
			}
			
		}
		return false;
	}
	//将headObj里的内容重新写入all.sch
	public void writetofile() throws IOException{
		//将all.sch文件清空
		File f=new File(filename);
		FileWriter fw=new FileWriter(f);
		fw.write("");
		fw.flush();
		fw.close();
		
		RandomAccessFile  file=new RandomAccessFile(filename,"rw");
		file.seek(0);
		file.writeBoolean(headObj.isStored);
		file.writeInt(headObj.lenOfTableNum);
		file.writeInt(headObj.offsetOfBody);
		file.close();
		
		
		for(int i=0;i<headObj.lenOfTableNum;i++){
			file=new RandomAccessFile(filename,"rw");
			file.seek(META_HEAD_SIZE+i*TABLE_NAME_ENTRY_LEN);
			file.write(headObj.tableNames.get(i).get(0).getBytes()); //写入表名
			file.writeInt(Integer.parseInt(headObj.tableNames.get(i).get(1))); //写入表的域个数
			file.writeInt(Integer.parseInt(headObj.tableNames.get(i).get(2))); //写入表的偏移
			file.seek(Integer.parseInt(headObj.tableNames.get(i).get(2)));
			List<List<String>> fields=new ArrayList<>();
			fields=headObj.tableFields.get(headObj.tableNames.get(i).get(0).trim()); //为当前这个表的域信息集合
			for(int j=0;j<Integer.parseInt(headObj.tableNames.get(i).get(1));j++){
				
				//写入域名信息
				file.write(fields.get(j).get(0).getBytes());
				//写入域类型信息
				file.writeInt(Integer.parseInt(fields.get(j).get(1)));
				//写入域长度信息
				file.writeInt(Integer.parseInt(fields.get(j).get(2)));
				
			}
			file.close();
		}	
	}
	//删除一张表，模式文件也要修改相关信息，主存的headObj也要作出修改,再对模式文件修改（直接调用上面的writetofile函数）
	public boolean delete_table_schema(String tablename) throws IOException{
		tablename=tablename.trim();
		int tmpindex=-1;
		for(int i=0;i<headObj.lenOfTableNum;i++){
			if(tablename.equals(headObj.tableNames.get(i).get(0).trim())){
				tmpindex=i; //获取这张表在tableNames里的索引
			}
		}
		//如果存在这张表
		if(tmpindex>=0){
			headObj.tableNames.remove(tmpindex);
			headObj.tableFields.remove(tablename);
			headObj.lenOfTableNum-=1;
			//删除这张表后，还有其他的表存在，则要修改其他表的偏移
			if (headObj.lenOfTableNum>0){
				Stream<List<String>> st=headObj.tableNames.stream();
				//下面生成了三个列表，分别代表表名列表、域个数列表、表偏移列表
				//map以后只包含指定转换函数转换后的元素
				List<String> tablenames=st.map((x)->x.get(0)).collect(Collectors.toList());
				st=headObj.tableNames.stream();
				List<String> fieldsnum=st.map((x)->x.get(1)).collect(Collectors.toList());
				st = headObj.tableNames.stream();
				List<String> offsets = st.map((x) -> x.get(2)).collect(Collectors.toList());
				//重新设置偏移列表的第一个元素值（可能删除的是第一张表）
				offsets.set(0, Integer.toString(BODY_BEGIN_INDEX));
				//从1下标开始，如果删除的是第一张表，则上面已经修改了第一张表的偏移，如果删除的不是第一张表，则第一张表也不需要动，
				for(int i=1;i<offsets.size();i++){
					//现在i表的偏移=前一个表的偏移+前一个表的域个数*最大域长度
					offsets.set(i, String.valueOf(Integer.parseInt(offsets.get(i-1))+Integer.parseInt(fieldsnum.get(i-1))*MAX_FIELD_LEN));
				}
				//对新的表名列表、域类型列表、域长度列表重新生成tableNames
				//headObj.tableNames.clear(); //先将原来的tableNames清空
				for(int i=0;i<headObj.lenOfTableNum;i++){
					//合并三个列表的第i个元素（代表第i张表）成为新的列表
					List<String> name=new ArrayList<>();
					name.add(tablenames.get(i));
					name.add(fieldsnum.get(i));
					name.add(offsets.get(i));
					headObj.tableNames.set(i,name);
				}
				//设置新的主体偏移，是最后一张表的offset加上最后一个表的域个数乘以域长度
				headObj.offsetOfBody=Integer.parseInt(headObj.tableNames.get(headObj.lenOfTableNum-1).get(2))+Integer.parseInt(headObj.tableNames.get(headObj.lenOfTableNum-1).get(1))*MAX_FIELD_LEN;
				writetofile();  //调用重新写入文件方法
			}
			else{
				System.out.println("删除该表后，没有表存在");
				headObj.offsetOfBody= BODY_BEGIN_INDEX;
				headObj.isStored=false;
				headObj.lenOfTableNum=0;
				writetofile();
			}
			return true;
		}
		else{
			System.out.println("无法找到该表");
			return false;
		}
	}
	//获取表名列表
	public List<String> get_tablenamelist(){
		Stream<List<String>> st=headObj.tableNames.stream();
		List<String> tablenames=st.map((x)->x.get(0).trim()).collect(Collectors.toList());
		return tablenames;
	}
	

}
	
	
	

