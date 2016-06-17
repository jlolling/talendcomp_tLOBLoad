package de.cimt.talendcomp.lob.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LOBUpload {
	
	private Map<String, SimpleDateFormat> sdfMap = new HashMap<String, SimpleDateFormat>();
	private Map<String, Object> valueMap = new HashMap<String, Object>();
	private Map<String, String> strValueMap = new HashMap<String, String>();
	private Connection connection;
	private String tableName;
	private String schemaName;
	
	public void reset() {
		valueMap.clear();
	}
	
	private SimpleDateFormat getSimpleDateFormat(String pattern) {
		if (pattern == null || pattern.trim().isEmpty()) {
			pattern = "yyyyMMdd_HHmmss";
		}
		pattern = pattern.trim();
		SimpleDateFormat sdf = sdfMap.get(pattern);
		if (sdf == null) {
			sdf = new SimpleDateFormat(pattern);
			sdfMap.put(pattern, sdf);
		}
		return sdf;
	}
	
	public void setValue(String columnName, Object value, String pattern) {
		if (value != null) {
			valueMap.put(columnName, value);
			strValueMap.put(columnName, convertToString(value, pattern));
		}
	}
	
	private String convertToString(Object value, String pattern) {
		String strValue = null;
		if (value instanceof Date) {
			strValue = getSimpleDateFormat(pattern).format((Date) value);
		} else if (value != null && (value instanceof Blob) == false && (value instanceof Clob) == false) {
			strValue = String.valueOf(value);
		} else {
			return null;
		}
		return strValue;
	}
	
	private String configurePath(String filePathTemplate) {
		StringReplacer sr = new StringReplacer(filePathTemplate);
		for (Map.Entry<String, String> entry : strValueMap.entrySet()) {
			if (entry.getValue() != null) {
				sr.replace("{" + entry.getKey() + "}", entry.getValue());
			} else {
				sr.replace("{" + entry.getKey() + "}", "");
			}
		}
		return sr.getResultText();
	}

	/**
	 * download the blob/clob content
	 * @param lobObject
	 * @param filePathTemplate
	 * @throws Exception
	 */
	public String uploadLob(Object lobObject, String filePathTemplate, String charset) throws Exception {
		if (lobObject != null) {
			File file = new File(configurePath(filePathTemplate));
			if (lobObject instanceof Blob) {
				FileInputStream is = new FileInputStream(file);
				Blob blob = (Blob) lobObject;
				OutputStream os = null;
				try {
					os = blob.setBinaryStream(1);
					final byte[] buffer = new byte[1024];
					int length = -1;
					while ((length = is.read(buffer)) != -1) {
						os.write(buffer, 0, length);
					}
				} finally {
					if (os != null) {
						os.flush();
						os.close();
					}
					if (is != null) {
						is.close();
					}
				}
			} else if (lobObject instanceof Clob) {
				Reader is = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
				Clob clob = (Clob) lobObject;
				Writer os = null;
				try {
					os = clob.setCharacterStream(1);
					final char[] buffer = new char[1024];
					int length = -1;
					while ((length = is.read(buffer)) != -1) {
						os.write(buffer, 0, length);
					}
				} finally {
					if (os != null) {
						os.flush();
						os.close();
					}
					if (is != null) {
						is.close();
					}
				}
			} else {
				throw new Exception("Given object is not a Blob or Clob. It is of type:" + lobObject.getClass().getName());
			}
			return file.getAbsolutePath();
		} else {
			return null;
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

}
