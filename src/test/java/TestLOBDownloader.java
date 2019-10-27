import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import de.cimt.talendcomp.lob.download.LOBDownload;


public class TestLOBDownloader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String DRIVER= "oracle.jdbc.driver.OracleDriver";
		String URL= "jdbc:oracle:thin:@//on-0337-jll.local:1521/XE";
		String USER="DWH_ODS";
		String PASSWORD="lolli";
		try {
			Class.forName(DRIVER);
			Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select ID, BIN_DATA,CLOB_DATA,date_value from BLOB_TEST");
			LOBDownload d = new LOBDownload();
			d.setCreateDir(true);
			while (rs.next()) {
				d.setValue("ID", rs.getObject(1), null);
				d.setValue("date", rs.getDate(4), "yyyy-MM-dd");
				d.downloadLob(rs.getObject(2), "/var/testdata/lob/blob/blob_{ID}_{date}.bin", null);
				d.downloadLob(rs.getObject(3), "/var/testdata/lob/clob/clob_{ID}.txt", "UTF-8");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
