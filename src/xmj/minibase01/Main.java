package xmj.minibase01;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
	public static final String choiceOption ="请输入你的选项: \n1:添加一个新的表结构和数据 \n2:删除一个表结构和数据" + 
			"\n3:查看表结构和数据 \n4:删除所有表和数据 \n5:输入sql语句进行查询" + 
			"\n6:根据字段关键字删除一行 \n7:根据字段关键字更新一行 \n. to quit):\n";
	public static String tableName = null;
	
	public static void main(String[] args) throws IOException {
		Schema schemaObj=new Schema();
		Storage dataObj=null;
		System.out.println(choiceOption);
		Scanner sc=new Scanner(System.in);
		int choice=sc.nextInt();
		while(true) {
			if (choice==1) //添加一个新的表结构和数据
			{
				System.out.println("现在执行添加一张表和插入记录的操作");
				System.out.println("请输入新添加的表名：");
				String tableName=sc.next().trim();
				List<List<String>> insertFieldList=new ArrayList<>();
				List<String> tablenamelists=schemaObj.get_tablenamelist();
				System.out.println(tablenamelists);
				if(!tablenamelists.contains(tableName)){ //该表不存在
					System.out.println("该表不存在，则添加一个表");
					dataObj=new Storage(tableName); //新建一个该表名对应的数据对象
					insertFieldList=dataObj.getFieldlist();  //获取域信息列表
					schemaObj.appendtable(tableName,insertFieldList); //向模式文件添加该表的信息
					
				}
				else{ //该表已经存在
					dataObj=new Storage(tableName);
					
					insertFieldList=dataObj.getFieldlist();
					while(true){
						List<String> record=new ArrayList<>(); //record是一个记录（保存有该表的数据）
						for (int i =0;i<insertFieldList.size();i++){
							System.out.println("第"+(i+1)+"个域属性名称："+insertFieldList.get(i).get(0)+",属性类型为："+insertFieldList.get(i).get(1)
									+",属性长度为："+insertFieldList.get(i).get(2));
							String a=sc.next();
							record.add(a);
						}
						if(dataObj.insert_record(record)){
							System.out.println("插入记录成功！");
						}
						else{
							System.out.println("输入错误！");
						}
						System.out.println("想继续插入记录请输入1，不想继续插入记录请输入2");
						int ch=sc.nextInt();
						if(ch==2){
							break;
						}
					}
					dataObj=null;	
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if(choice==2){ //从模式文件里删除一张表结构和他的数据
				System.out.println("现在执行删除一张表和其数据的操作");
				System.out.println("请输入要删除的表名");
				String tablename=sc.next();
				tablename=tablename.trim();
				if(schemaObj.find_table(tablename)){ //找到这张表
					if(schemaObj.delete_table_schema(tablename)){ //在模式文件里删除成功
						dataObj=new Storage(tablename);
						dataObj.deletetabledata(tablename); //删除这张表的数据
						dataObj=null;
					}
					else{
						System.out.println("从模式文件里删除这张表失败！");
					}
				}
				else{
					System.out.println("不存在 表"+tablename);
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
				
			}
			else if(choice==3){ //查看表结构和数据
				System.out.println("现在执行查看表结构和数据的操作");
				List<String> tablenamelist=schemaObj.get_tablenamelist();
				System.out.println("所有的表名如下：");
				System.out.println(tablenamelist);
				System.out.println("请输入要查看的表名");
				String tablename=sc.next();
				tablename=tablename.trim();
				if(schemaObj.find_table(tablename)){
					schemaObj.viewTableStructure(tablename);
					dataObj=new Storage(tablename);
					dataObj.show_tabledata();
					dataObj=null;
				}
				else{
					System.out.println("不存在表"+tablename);
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if(choice==4){ //删除所有表和数据
				System.out.println("现在执行删除所有表和数据的操作");
				List<String> tablenamelist=schemaObj.get_tablenamelist();
				for(int i=0;i<tablenamelist.size();i++){
					String tablename=tablenamelist.get(i).trim();
					dataObj=new Storage(tablename);
					dataObj.deletetabledata(tablename);
					dataObj=null;
				}
				schemaObj.deleteAll(); //删除模式文件
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if(choice==5){
				System.out.println("现在输入一个SQL查询语句：");
				String str1=sc.nextLine();
				String sqlstr=sc.nextLine(); //获取带空格的字符串
				Judge ju=new Judge();
				Node nodeobj=ju.judge(sqlstr); //调用judge函数，得到语法树
				nodeobj.show(nodeobj);
				ParseNode2 p2=new ParseNode2();
				p2.construct_logical_tree();
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if(choice==6){//根据字段关键字删除一行
				System.out.println("现在执行根据关键字删除表的一行记录");
				System.out.println("请输入要删除的表名：");
				String tablename=sc.next();
				tablename=tablename.trim();
				if(schemaObj.find_table(tablename)){
					//如果找到该表
					dataObj=new Storage(tablename);
					System.out.println("要删除的记录的关键字取值为：");
					String field_value=sc.next();
					if(dataObj.delete_record(field_value))
					{
						System.out.println("删除该条记录成功！");
					}
					else{
						System.out.println("不存在该关键字对应的记录，删除失败！");
					}
				}
				else{
					System.out.println("不存在该表！");
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if (choice==7){
				//根据字段关键字更新一行
				System.out.println("现在执行根据输入的关键字更新表的一条记录");
				System.out.println("请输入要更新的记录的表名：");
				String tablename=sc.next();
				tablename=tablename.trim();
				if(schemaObj.find_table(tablename)){
					dataObj=new Storage(tablename);
					System.out.println("请输入要更新的记录的关键字取值：");
					String field_value=sc.next();
					if(dataObj.update_record(field_value)){
						System.out.println("成功更新记录！");
					}
					else{
						System.out.println("不能找到输入的关键字对应的记录，更新失败！");
					}
					
				}
				else{
					System.out.println("不能找到该表！");
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else{
				
			}
		}
	}
	
}
