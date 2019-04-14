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
	public List<List<String>> record_list=new ArrayList<>(); //��¼�б�
	public List<List<Integer>> record_position=new ArrayList<>(); //��¼λ���б�
	public List<List<String>> field_name_list=new ArrayList<>();  //�����б�
	public String path="D:\\minibase_data\\table\\";
	public String tableName=null;
	public RandomAccessFile file=null;
	public boolean open;
	public int num_of_fields;
	public Scanner sc=new Scanner(System.in);
	public int block_id=0; //��id
	public int data_block_num=0; //���ݿ�ĸ���
	public int num_of_records=0;
	public int recordheadlen=18;
	public int record_len=0;
	
	public  String fillTablename(String tableName){
		if(tableName.trim().length()<10){
			tableName = tableName.format("%10s", tableName);// ������ʽ��
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
			System.out.println(tablename.trim()+"��ɾ���ɹ���");
			return true;
		}
		return false;
	}
	public Storage(String tablename) throws IOException{
		this.tableName=tablename.trim();
		File f=new File(this.path+this.tableName+".dat");
		if(!f.exists()){
			System.out.println("�ļ�"+this.tableName+".dat������");
			//�ļ������ڣ��򴴽�һ���ļ�
			f.createNewFile();
			this.open=false;
			System.out.println("�ļ�"+this.tableName+".dat�Ѿ�������");			
		}
		file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
		System.out.println("���ļ�"+this.tableName+".dat");
		this.open=true;
		byte[] dir_buf=new byte[BLOCK_SIZE];
		file.seek(0);
		int mylen=file.read(dir_buf); //�ӱ��ļ����ȡBLOCK_SIZE��С������
		file.close();
		int begin_index=0;
		
		if(mylen==-1) //�����ļ���û�����ݣ����½�����
		{
			this.data_block_num+=1;
			System.out.println("�������������");
			this.num_of_fields=sc.nextInt();
			if(this.num_of_fields>0)//������������0
			{
				file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
				file.seek(0);
				file.writeInt(0); //д��block_id
				file.writeInt(this.data_block_num);
				file.writeInt(this.num_of_fields);
				
				begin_index+=12; //ͷ�������ֶ�ռ��12�ֽ�
				
				for (int i = 0; i < this.num_of_fields; i++) {
					System.out.println("������������");
					String field_name = sc.next();
					if (field_name.length() < 10) {
						field_name = fillTablename(field_name);
					}
					int field_type;
					while (true) {
						System.out.println("�����������ͣ���0����str��1����int��2����booLean��");
						field_type = sc.nextInt();
						if (field_type == 0 || field_type == 1 || field_type == 2)
							break;
						System.out.println("����������!");
					}
					System.out.println("�������򳤶ȣ�");
					int fieldlen = sc.nextInt();
					List<String> temp_list = new ArrayList<>();
					temp_list.add(field_name);
					temp_list.add(Integer.toString(field_type));
					temp_list.add(String.valueOf(fieldlen));
					this.field_name_list.add(temp_list); // ��[�����������ͣ��򳤶�]�б����field_name_list

					// ������Ϣд�������ļ�
					
					file.seek(begin_index);
					file.write(field_name.getBytes());
					file.writeInt(field_type);
					file.writeInt(fieldlen);
					begin_index += Schema.MAX_FIELD_LEN;

				}
				
			}
			file.close();
		}
		else //�����ļ���������
		{
			file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
			file.seek(0);
			this.block_id=file.readInt();
			this.data_block_num=file.readInt();
			this.num_of_fields=file.readInt();
			System.out.println("��ǰ���ݿ�Ϊ��"+this.block_id+",���ݿ����Ϊ��"+this.data_block_num+",�����Ϊ��"+this.num_of_fields);
			begin_index+=12;
			for(int i=0;i<this.num_of_fields;i++){
				file.seek(begin_index);
				byte[] name=new byte[10];
				file.read(name); //��ȡ10���ֽڵ�����
				String field_name=new String(name); //�����ֽ����鴴��һ���ַ���
				int field_type=file.readInt();
				int field_len=file.readInt();
				List<String> temp_list=new ArrayList<>();
				temp_list.add(field_name);
				temp_list.add(Integer.toString(field_type));
				temp_list.add(String.valueOf(field_len));
				this.field_name_list.add(temp_list); //������Ϣ����field_name_list
				System.out.println("��"+(i+1)+"�����Ϣ���£�����:"+field_name+"��������:"+field_type+"���򳤶�"+field_len);
				begin_index+=18;
			}
			file.close();
		}
		
		//�����������record_List,��record_position��
		Stream<List<String>> st=this.field_name_list.stream();
		//��ȡÿ����ĳ��ȣ����һ���б�
		List<String> fieldlen_list=st.map((x)->x.get(2)).collect(Collectors.toList());
		int record_content_len=0; //����ÿ����¼�ĳ��ȣ�ÿ����ĳ���֮��
		for(int i=0;i<fieldlen_list.size();i++){
			record_content_len+=Integer.parseInt(fieldlen_list.get(i));
		}
		
		int flag=1;
		while(flag<this.data_block_num) //�������е����ݿ�
		{
			begin_index=BLOCK_SIZE*flag;  //��ʼ����ָ���һ�����ݿ�Ŀ�ͷ��block_0��
			file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
			file.seek(begin_index);
			this.block_id=file.readInt(); //��ȡ��id
			this.num_of_records=file.readInt();  //��ȡ��¼��
			System.out.println("��ǰ��ID="+this.block_id+",��"+this.num_of_records+"����¼");
			//����м�¼���ڣ��ͽ���¼���뵽record_list��record_position��ȥ
			if(this.num_of_records>0){
				for(int i=0;i<this.num_of_records;i++){
					List<Integer> temp_pos=new ArrayList<>();
					temp_pos.add(flag);
					temp_pos.add(i);
					this.record_position.add(temp_pos);
					
					file.seek(begin_index+8+i*4);  //�ҵ�����¼i��ƫ�ơ�����λ��
					int offset=file.readInt(); //����¼i��ƫ��
					int record_des=offset+this.recordheadlen;  //���Ǹü�¼�������ļ���λ�ã���¼i��ƫ�ƣ��ڱ����ڵ����λ�ƣ�+begin_index�������ڸ��������ʼ��ַ��+��¼��ͷ����
					file.seek(record_des+begin_index); //�ҵ���¼i
					//�����������record_list
					List<String> temp_list=new ArrayList<>();  //�Ǽ�¼�б��б����ÿ��Ԫ���Ǹ������ȡֵ
					for(int j=0;j<this.num_of_fields;j++){
						byte[] attr=new byte[Integer.parseInt(this.field_name_list.get(j).get(2))];
						file.read(attr); //ȥ���ֽ�����
						String attr1=new String(attr); //attr1������ǵ�j������ȡ��ֵ
						temp_list.add(attr1); 
					}
					this.record_list.add(temp_list);
				}
				
			}
			flag+=1;
			file.close();
		}
		
		
	}
	//��ȡ���еļ�¼
	public List<List<String>> getrecord(){
		return this.record_list;
	}
	
	//����һ����¼
	public boolean insert_record(List<String> insertrecord) throws IOException{
		List<String> temprecord=new ArrayList<>(); 
		String pk=insertrecord.get(0);
		int ispk=-1;
		for(int i=0;i<this.record_list.size();i++){
			if(pk.equals(this.record_list.get(i).get(0).trim())){
				System.out.println("�����¼������ֵ�ڱ����Ѿ����ڣ����ɲ��룡");
				ispk=i;
				break;
			}
		}
		if(ispk<0){// ������������������Բ��������¼
			for(int i=0;i<this.num_of_fields;i++){
				insertrecord.set(i, insertrecord.get(i).trim()); //�������¼����ȡֵȥ���ո�
				if(Integer.parseInt(this.field_name_list.get(i).get(1))==0) //�����������Ϊstr����varstr
				{
					if(insertrecord.get(i).length()>Integer.parseInt(this.field_name_list.get(i).get(2))) //��������¼�ڸ����ϵĳ��ȳ����˹涨���򳤶�
					{
						return false;
					}
					temprecord.add(insertrecord.get(i)); //���ϳ���Ҫ���������ʱ��¼�б�
				}
				if(Integer.parseInt(this.field_name_list.get(i).get(1))==1)//��int����
				{
					try{
						temprecord.add(insertrecord.get(i));	
					}
					catch (Exception e) {
						return false;
					}	
				}
				if(Integer.parseInt(this.field_name_list.get(i).get(1))==2) //��boolean����
				{
					try{
						temprecord.add(insertrecord.get(i));	
					}
					catch (Exception e) {
						return false;
					}	
				}
				//����������ȡֵ�ĳ��Ȳ�����涨�ĳ���ʱ��ȫ��ǰ������Ͽո�
				int fill_length=Integer.parseInt(this.field_name_list.get(i).get(2))-temprecord.get(i).length(); //�õ����Ȳ�ֵ
				if(fill_length<0){ //����С��0������ȡֵ�����˸���Ĺ涨����
					return false;
				}
				String strb=""; //�ַ���ƴ��ʱ����ʹ��stringbuffer
				for(int j=0;j<fill_length;j++){
					strb+=" ";
				}
				//��insertrecord���ÿ����ȡֵ��ȫ�������󳤶�
				temprecord.set(i, strb+temprecord.get(i).trim());
				
			}//���ˣ�temprecord�����Ƿ��ϳ���Ҫ�����ȡֵ
			
			System.out.println(temprecord);
			StringBuffer recordstr=new StringBuffer(); //��ȡ�����¼���ÿ����ȡֵ����Ϊ�ַ���
			for(int j=0;j<this.num_of_fields;j++){
				recordstr.append(temprecord.get(j)); //recordstr���ַ���������ȡֵ�б�ת�����ַ���
			}
			System.out.println("��ǰҪ����ļ�¼Ϊ��"+ new String(recordstr));
			this.record_list.add(temprecord); //�������¼���뵽record_list��ȥ
			System.out.println("��ǰ��¼�б�"+this.record_list);
			//�������޸�record_position �����һ��Ԫ�أ����¼�¼��λ�ã�
			//����������ݿ�������¼��
			int recordcontentlen=recordstr.toString().length(); //��¼��ĳ���
			this.record_len=recordcontentlen+this.recordheadlen; //��¼�ĳ��ȣ�������¼ͷ�ͼ�¼�壩
			int MAX_RECORD_NUM=(BLOCK_SIZE-8)/(4+this.record_len); //-8��ȥ��һ����ǰ���block id�ͼ�¼�������ֶΣ�4+record_len��ÿ����¼��ƫ�ƺͼ�¼����
			List<Integer> temp=new ArrayList<>();
			//��������µļ�¼��λ��
			if(this.record_position.size()==0){ //ԭ��û�м�¼����
				System.out.println("ԭ��û�����ݴ���");
				this.data_block_num+=1; //���ݿ���+1
				temp=new ArrayList<>();
				temp.add(1);
				temp.add(0);
				this.record_position.add(temp); //��[1,0]����record_position
				
			}
			else{  //ԭ���м�¼����
				System.out.println("ԭ�������ݴ���");
				List<Integer> last_pos=new ArrayList<>();
				last_pos=this.record_position.get(this.record_position.size()-1); //��ȡ���һ����¼��λ��
				if(last_pos.get(1)==MAX_RECORD_NUM-1) 
				//��һ����¼�Ѿ���������һ�����ݿ�����һ��λ���ϣ�������������ݿ��Ѿ��������ٴ��������
				{
					System.out.println("������ݿ��Ѿ�����������һ�����ݿ�");
					temp=new ArrayList<>();
					temp.add(last_pos.get(0)+1); //��ŵ����ݿ�id+1
					temp.add(0);
					this.record_position.add(temp); //��λ�ü���record_position
					this.data_block_num+=1;  //��ʹ����һ�����ݿ飬�������ݿ����+1
				}
				else  //������ݿ鲻�����Կ��Բ����¼
				{
					System.out.println("������ݿ黹�п���");
					temp=new ArrayList<>();
					temp.add(last_pos.get(0));
					temp.add(last_pos.get(1)+1);
					this.record_position.add(temp);
				}
				
			}
			List<Integer> last_pos=new ArrayList<>();
			System.out.println("���������ļ�����"+this.record_position);
			last_pos=this.record_position.get(this.record_position.size()-1);
			System.out.println("���һ����¼��λ�ã�"+last_pos);
			//��������xxx.dat�ļ���Ӳ���ļ�¼
			//����data_block_num�������ļ���ͷ ��block0����data_block_num��
			file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
			file.seek(0);
			file.writeInt(0);
			file.writeInt(this.data_block_num);
			file.writeInt(this.num_of_fields);
			//���¼�¼��num_of_records
			file.seek(BLOCK_SIZE*last_pos.get(0)); //��λ�����һ����¼�Ŀ鿪ʼ��
			file.writeInt(last_pos.get(0)); //��id���ֲ���
			file.writeInt(last_pos.get(1)+1); //���¼�¼��
			
			
			//����offset�������¼ƫ��
			int offset=BLOCK_SIZE*last_pos.get(0)+8+4*last_pos.get(1); //ָ�����һ����¼λ�ã����λ�������һ����¼��ƫ�ƣ�
			int begin_index=BLOCK_SIZE-this.record_len*(last_pos.get(1)+1); //ָ�����һ����¼��ʵ���ڵ�λ�ò��Ǽ�¼��ƫ�ƴ��λ��
			file.seek(offset); //�ҵ���¼ƫ�ƴ�
			file.writeInt(begin_index); //����¼��ƫ��д���ļ�
			
			//�������ݣ������¼
			Date updatetime=new Date(); //��ȡ��ǰʱ��
			SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-mm-dd"); //����ʱ���ʽ
			String strdate=dateformat.format(updatetime); //ת�����ַ���
			file.seek(BLOCK_SIZE*last_pos.get(0)+begin_index+4); //ָ���Ҽ�¼ʵ��Ҫ�����λ�ã�+4�Ǽ�¼ͷ����pointer��ռ4�ֽڣ�������12�ֽ�ô��
			file.writeInt(this.record_len); //д���¼�ĳ���
			file.write(strdate.getBytes()); //д����µ�ʱ��
			file.write(recordstr.toString().getBytes()); //д���¼����
			file.close();
			return true;
			
		}
		return false;
		
	}
	
	//ɾ��һ����¼
	public boolean delete_record(String primarkeyvalue) throws IOException {
		primarkeyvalue=primarkeyvalue.trim();
		int pos = -1;
		int record_offset = 0; // �ü�¼�洢λ��
		int blockid = 0;
		int recordsnum = 0;
		int recordpos = 0;
		int recordlen=0;
		for(int i=0;i<this.field_name_list.size();i++){
			recordlen+=Integer.parseInt(this.field_name_list.get(i).get(2));
		}
		List<Integer> temp = new ArrayList<>();
		for (int i = 0; i < this.record_list.size(); i++)// �����еļ�¼���鿴��������ֵ�ļ�¼�Ƿ����
		{
			if (primarkeyvalue.equals(this.record_list.get(i).get(0).trim())) {
				pos = i; // �ҵ�������¼����������λ��i
			}
		}
		if (pos == -1) {
			System.out.println("������¼�����ڣ�");
			return false;
		} else {
			
			System.out.println("Ҫɾ���ļ�¼Ϊ"+this.record_list.get(pos));
			this.record_list.remove(pos); // ɾ������±���ļ�¼
			
			temp = this.record_position.get(pos); // ��ȡ������¼��λ��[block id,offset]
			blockid = temp.get(0);
			recordpos = temp.get(1);
			System.out.println("Ҫɾ���ļ�¼�ڵ�" + blockid + "�飬Ҫɾ���ļ�¼�ǵ�" + (recordpos+1) + "����¼��");
			this.record_position.remove(pos); // ɾ������±괦�ļ�¼λ��
			
			file = new RandomAccessFile(this.path + this.tableName + ".dat", "rw");
			file.seek(BLOCK_SIZE * blockid + 4); // �ҵ������¼���ڿ�ļ�¼��
			recordsnum = file.readInt(); // ��ȡ�ÿ�ļ�¼��
			file.close();

			if (recordsnum == 1) { // ���ֻ��Ҫɾ������һ����¼���ڣ���ɾ�������
				
				this.data_block_num -= 1; // ����-1
				file = new RandomAccessFile(this.path + this.tableName + ".dat", "rw");
				file.seek(0);
				file.writeInt(0);
				file.writeInt(this.data_block_num);
				this.num_of_records -= 1; // ��¼��-1
				file.close();
			} else {
				this.num_of_records -= 1; // ��¼��-1
				
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
				int offset1=file.readInt(); //��ȡ�ƶ���¼��ƫ�Ƶ�ַ
				file.seek(BLOCK_SIZE*blockid+offset1+this.recordheadlen);
				byte[] record=new byte[recordlen];
				file.read(record);
				System.out.println("��ǰ�ƶ��ļ�¼Ϊ"+new String(record));
				int off1=BLOCK_SIZE*blockid+8+(p-1)*4;
				file.seek(off1);
				int offset2=file.readInt(); //��ȡ��ǰһ����¼��ƫ��
				file.seek(BLOCK_SIZE*blockid+offset2+this.recordheadlen);
				file.write(new String(record).getBytes());
			}
			file.close();
			System.out.println("��¼ɾ���ɹ���");
			return true;
			
			//Ҫɾ���ļ�¼����ļ�¼��Ҫ�ƶ�
			/*for (int p = recordpos; p < this.num_of_records; p++) 
			{//record_offsetָ��Ҫ�����¼Ҫ�����λ��
				record_offset = BLOCK_SIZE - (p + 1) * this.record_len;  //ָ��Ҫɾ���ļ�¼��λ��
				file.seek(BLOCK_SIZE*blockid+record_offset-this.record_len+this.recordheadlen); //ָ��Ҫɾ���ļ�¼��ǰһ����¼λ��
				byte[] fields=new byte[recordlen]; //һ����¼�ܳ��ȵ�����
				file.read(fields);
				System.out.println("����Ҫ�ƶ��ļ�¼��"+new String(fields));
				file.seek(BLOCK_SIZE*blockid+record_offset+this.recordheadlen);
				file.write(new String(fields).getBytes());
				*/
				
				//�ҵ�����һ����¼�ļ�¼�����ڵ�λ�ã����ʽ����+ this.recordheadlen��Ϊ�˿����¼ͷ��
				/*int field_len=0;
				for (int k = 0; k < this.field_name_list.size(); k++) {
					file.seek(BLOCK_SIZE * blockid + record_offset - this.record_len + this.recordheadlen); // �ҵ�Ҫɾ���ļ�¼����һ����¼�����λ��
					
					//byte�����Ǹ���ÿ����ĳ������ɵ�
					byte[] fieldvalue = new byte[Integer.parseInt(field_name_list.get(k).get(2))]; // ���ɵ�k�����Ӧ���ȵ��ֽ�����
					file.read(fieldvalue); // ��ȡ��ȡֵ
					
					file.seek(BLOCK_SIZE * blockid + record_offset + this.recordheadlen+field_len); // ָ������¼Ҫ�����λ��
					file.write(new String(fieldvalue).getBytes());
					field_len+=Integer.parseInt(field_name_list.get(k).get(2)); //field_len����򳤶��ۼ�
				}*/
			//}
			/*file.close();
			System.out.println("��¼ɾ���ɹ���");
			return true;*/
		}
	}
	
	//���¼�¼
	public boolean update_record(String primarykeyvalue) throws IOException{
		primarykeyvalue=primarykeyvalue.trim();
		int pos=-1; //posָ��������¼��λ��
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
			System.out.println("������������¼��");
			return false;
		}
		else{
			//�������
			System.out.println("�ñ�������У�");
			for(int i=0;i<this.field_name_list.size();i++){
				System.out.print(i+":"+this.field_name_list.get(i).get(0)+"\t");
			}
			int up_index=-1; //up_indexָ��Ҫ�޸ĵ��ǵڼ�����
			while(true){ //ȷ������������Ƿ���Ҫ��ģ����������Χ�ڣ�
				System.out.println("��������Ҫ�ı�����������(0-"+(this.field_name_list.size()-1)+")");
				up_index=sc.nextInt();
				//��������up_index�Ƿ�Ϸ�
				for(int k=0;k<this.field_name_list.size();k++){
					if(up_index==k)
						break;
				}
				if(up_index!=-1)
					break;
			}
			
			int field_type=Integer.parseInt(this.field_name_list.get(up_index).get(1)); //��ȡҪ�޸ĵ��������
			int field_len=Integer.parseInt(this.field_name_list.get(up_index).get(2));  //��ȡҪ�޸ĵ���ĳ���
			System.out.println("���������Ϊ��"+field_type+"����ĳ���Ϊ��"+field_len);
			
			//����ȷ���������ֵ���ȺϷ�
			while(true){
				System.out.println("����������޸ĺ��ֵ��");
				newvalue=sc.next();
				if(newvalue.length()>field_len){
					System.out.println("�������ȡֵ�����涨���ȣ�����");
				}
				else
					break;
			}
			//���������ȡֵ��ʹ����Ϊ�涨����
			String space="";
			for(int t=0;t<field_len-newvalue.length();t++){
				space+=" ";
			}
			newvalue=space+newvalue;
			List<String> record=this.record_list.get(pos); //��ȡҪ�޸ĵļ�¼
			System.out.println("�޸�ǰ�ļ�¼Ϊ��"+record);
			record.set(up_index, newvalue); //�޸ļ�¼����򴦵�ֵ
			System.out.println("�޸��Ժ�ļ�¼Ϊ��"+record);
			this.record_list.set(pos,record);
			int totallen=0; //ǰ��û�иĶ�����ĳ���֮��
			for(int i=0;i<up_index;i++){
				totallen+=Integer.parseInt(this.field_name_list.get(i).get(2));
			}
			//�����޸��ļ���ļ�¼������
			List<Integer> temp=new ArrayList<>();
			temp=this.record_position.get(pos); //��ȡҪ�޸ĵļ�¼��λ��
			blockid=temp.get(0);  //��ȡ������¼�Ŀ�id 
			record_off=temp.get(1); //��ȡ������¼����������ƫ��
			System.out.println("Ҫ�޸ĵļ�¼λ��"+blockid+"�飬�ǵ�"+record_off+"����¼��");
			file=new RandomAccessFile(this.path+this.tableName+".dat","rw");
			
			file.seek(BLOCK_SIZE*blockid+8+record_off*4);
			int off=file.readInt();
			file.seek(BLOCK_SIZE*block_id+off+this.recordheadlen+totallen);
			file.write(newvalue.getBytes());
			
			/*file.seek(BLOCK_SIZE*blockid+BLOCK_SIZE-(pos+1)*this.record_len+this.recordheadlen+totallen); //ָ��Ҫ�޸ĵ���λ��
			file.write(newvalue.getBytes()); //���µ���ȡֵд���ļ�
*/			file.close();
			System.out.println("��¼�޸ĳɹ���");
			return true;
		}
	}
	
	
	//չʾ������ݣ���¼��
	public void show_tabledata(){
		List<String> fieldname=new ArrayList<>(); //��������
		for(int i=0;i<this.field_name_list.size();i++){
			fieldname.add(this.field_name_list.get(i).get(0).trim());
		}
		System.out.println("�ñ���������£�");
		for(int i=0;i<fieldname.size();i++){
			if(i!=fieldname.size()-1){ //û�е����һ����
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
	
	/*//ɾ���ñ��ļ�
	public boolean delete_tabledata(String tablename){
		if(this.open==true) //�������ļ�������
		{
			this.open=false;
		}
		tablename=tablename.trim();
		File f=new File(this.path+tablename+".dat");
		if(f.exists()){
			//������ļ�����
			f.delete(); //ɾ�����ļ�
		}
		return true;
	}*/
	//��ȡ����Ϣ�б�
	public List<List<String>> getFieldlist(){
		return this.field_name_list;
	}
		
}

