/**
 * Copyright 2015 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cimt.talendcomp.lob.download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LOBDownload {
	
	private Map<String, SimpleDateFormat> sdfMap = new HashMap<String, SimpleDateFormat>();
	private Map<String, String> valueMap = new HashMap<String, String>();
	private boolean createDir = false;
	
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
		String strValue = null;
		if (value instanceof Date) {
			strValue = getSimpleDateFormat(pattern).format((Date) value);
		} else if (value != null && (value instanceof Blob) == false && (value instanceof Clob) == false) {
			strValue = String.valueOf(value);
		}
		if (strValue != null) {
			valueMap.put(columnName, strValue);
		}
	}
	
	private String configurePath(String filePathTemplate) {
		StringReplacer sr = new StringReplacer(filePathTemplate);
		for (Map.Entry<String, String> entry : valueMap.entrySet()) {
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
	public String downloadLob(Object lobObject, String filePathTemplate, String charset) throws Exception {
		if (lobObject != null) {
			File file = new File(configurePath(filePathTemplate));
			if (createDir && file.getParentFile().exists() == false) {
				file.getParentFile().mkdirs();
			}
			if (lobObject instanceof Blob) {
				FileOutputStream os = new FileOutputStream(file);
				Blob blob = (Blob) lobObject;
				InputStream is = null;
				try {
					is = blob.getBinaryStream();
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
				Writer os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
				Clob clob = (Clob) lobObject;
				Reader is = null;
				try {
					is = clob.getCharacterStream();
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
			} else if (lobObject instanceof String) {
				Writer os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
				Reader is = new StringReader((String) lobObject);
				try {
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
			} else if (lobObject instanceof Byte[]) {
				FileOutputStream os = new FileOutputStream(file);
				try {
				    Byte[] array = (Byte[]) lobObject;
				    for (Byte b : array) {
					    os.write(b);
				    }
				} finally {
					if (os != null) {
						os.flush();
						os.close();
					}
				}
			} else if (lobObject instanceof byte[]) {
				FileOutputStream os = new FileOutputStream(file);
				try {
				    byte[] array = (byte[]) lobObject;
				    os.write(array);
				} finally {
					if (os != null) {
						os.flush();
						os.close();
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

	public boolean isCreateDir() {
		return createDir;
	}

	public void setCreateDir(boolean createDir) {
		this.createDir = createDir;
	}
	
}
