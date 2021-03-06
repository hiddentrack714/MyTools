package com.track.mytools.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelUtil {
	public static List<HashMap<String, Object>> readExcel(String fileName) {

		StringBuffer sb = new StringBuffer();
		Workbook wb = null;
		try {
			// 构造Workbook（工作薄）对象
			wb = Workbook.getWorkbook(new File(fileName));
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (wb == null)
			return null;

		// 获得了Workbook对象之后，就可以通过它得到Sheet（工作表）对象了
		Sheet[] sheet = wb.getSheets();

		List<HashMap<String, Object>> xlsList = new ArrayList<HashMap<String, Object>>();

		if (sheet != null && sheet.length > 0) {
			// 对每个工作表进行循环
			for (int i = 0; i < sheet.length; i++) {
				// 得到当前工作表的行数
				int rowNum = sheet[i].getRows();

				for (int j = 0; j < rowNum; j++) {
					// 得到当前行的所有单元格

					Cell[] cells = sheet[i].getRow(j);

					if (cells != null && cells.length > 0) {
						HashMap<String, Object> rowMap = new HashMap<String, Object>();
						// 对每个单元格进行循环
						for (int k = 0; k < cells.length; k++) {
							// 读取当前单元格的值
							String cellValue = cells[k].getContents();

							switch (k) {
								case 0:

									rowMap.put("pwdName", cellValue);
									break;
								case 1:

									rowMap.put("pwdAccount", cellValue);
									break;

								case 2:

									rowMap.put("pwdPsd", cellValue);
									break;

								default:
									rowMap.put("pwdIcon", getRealVal(cellValue,1));
									break;
							}
						}

						xlsList.add(rowMap);
					}

				}

			}
		}
		// 最后关闭资源，释放内存
		wb.close();
		xlsList.remove(0);
		return xlsList;
	}


	public static boolean saveExcel(List<HashMap<String, Object>> list,String fileName){
		Workbook wb = null;
		WritableWorkbook book = null;
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(fileName));
			// 构造Workbook（工作薄）对象
			book = Workbook.createWorkbook(os);

			WritableSheet sheet = book.createSheet("密码本", 0);

			sheet.addCell(new jxl.write.Label(0, 0, "名称"));
			sheet.addCell(new jxl.write.Label(1, 0, "账号"));
			sheet.addCell(new jxl.write.Label(2, 0, "密码"));
			sheet.addCell(new jxl.write.Label(3, 0, "分类"));


			for (int i = 0; i < list.size(); i++) {
				HashMap<String, Object> map = list.get(i);
				sheet.addCell(new jxl.write.Label(0, i+1, map.get("pwdName").toString()));
				sheet.addCell(new jxl.write.Label(1, i+1, map.get("pwdAccount").toString()));
				sheet.addCell(new jxl.write.Label(2, i+1, map.get("pwdPsd").toString()));
				sheet.addCell(new jxl.write.Label(3, i+1, getRealVal(map.get("pwdIcon").toString(),0)));
			}
			book.write();
			book.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 根据字母或者汉字获取对应的反向值
	 *
	 * @param str
	 *
	 * @param flag 0:字母,1:汉字
	 *
	 * @return
	 */
	public static String getRealVal(String str,int flag){
		if("出行".equals(str) || "cx".equals(str)){
			str = flag == 0 ? "出行" : "cx";
		}else if("购物".equals(str) || "gw".equals(str)){
			str = flag == 0 ? "购物" : "gw";
		}else if("金融".equals(str) || "jr".equals(str)){
			str = flag == 0 ? "金融" : "jr";
		}else if("游戏".equals(str) || "yx".equals(str)){
			str = flag == 0 ? "游戏" : "yx";
		}else if("生活".equals(str) || "sh".equals(str)){
			str = flag == 0 ? "生活" : "sh";
		}else if("社交".equals(str) || "sj".equals(str)){
			str = flag == 0 ? "社交" : "sj";
		}else if("影视".equals(str) || "ys".equals(str)){
			str = flag == 0 ? "影视" : "ys";
		}else{
			str = flag == 0 ? "其他" : "qt";
		}

		return str;
	}

	public static void main(String[] args) {
		List<HashMap<String, Object>> list = readExcel("C:\\Users\\Track\\Desktop\\密码本.xls");
		System.out.println(list.get(1).toString());
	}
}
