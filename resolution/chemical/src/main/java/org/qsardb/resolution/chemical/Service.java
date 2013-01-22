/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.conn.*;
import org.apache.http.impl.client.*;
import org.apache.http.params.*;
import org.apache.http.util.*;

public class Service {

	private Service(){
	}

	static
	public List<String> cas(String structure) throws IOException {
		return split(resolve(structure, "cas"));
	}

	static
	public String formula(String structure) throws IOException {
		return trim(resolve(structure, "formula"));
	}

	static
	public String iupac_name(String structure) throws IOException {
		return trim(resolve(structure, "iupac_name"));
	}

	static
	public List<String> names(String structure) throws IOException {
		return split(resolve(structure, "names"));
	}

	static
	public String sdf(String structure) throws IOException {
		return trim(resolve(structure, "sdf"));
	}

	static
	public String smiles(String structure) throws IOException {
		return trim(resolve(structure, "smiles"));
	}

	static
	public String stdinchi(String structure) throws IOException {
		return trim(resolve(structure, "stdinchi"));
	}

	static
	public String stdinchikey(String structure) throws IOException {
		return trim(resolve(structure, "stdinchikey"));
	}

	/*
	 * @throws FileNotFoundException If the structure is not found
	 * @throws IOException If an I/O exception occurs
	 */
	static
	private String resolve(String structure, String representation) throws IOException {
		String encodedStructure = URLEncoder.encode(structure, "UTF-8");

		URL url = new URL("http://cactus.nci.nih.gov/chemical/structure/" + encodedStructure.replace("+", "%20") + "/" + representation);

		HttpParams parameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(parameters, 10 * 1000);
		HttpConnectionParams.setSoTimeout(parameters, 2 * 1000);

		HttpClient client = new DefaultHttpClient(parameters);

		try {
			HttpGet request = new HttpGet(url.toURI());

			HttpResponse response = client.execute(request);

			StatusLine status = response.getStatusLine();

			switch(status.getStatusCode()){
				case HttpStatus.SC_OK:
					break;
				case HttpStatus.SC_NOT_FOUND:
					throw new FileNotFoundException(structure);
				default:
					throw new IOException(status.getReasonPhrase());
			}

			ByteArrayOutputStream os = new ByteArrayOutputStream(10 * 1024);

			try {
				HttpEntity responseBody = response.getEntity();

				try {
					responseBody.writeTo(os);
				} finally {
					os.flush();
				}

				String encoding = EntityUtils.getContentCharSet(responseBody);
				if(encoding == null){
					encoding = "UTF-8";
				}

				return os.toString(encoding);
			} finally {
				os.close();
			}
		} catch(URISyntaxException use){
			throw new IOException(use);
		} finally {
			ClientConnectionManager connectionManager = client.getConnectionManager();

			if(connectionManager != null){
				connectionManager.shutdown();
			}
		}
	}

	static
	private String trim(String string){

		if(string.endsWith("\n")){
			string = string.substring(0, string.length() - 1);
		}

		return string;
	}

	static
	private List<String> split(String string) throws IOException {
		List<String> lines = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new StringReader(string));

		try {
			while(true){
				String line = reader.readLine();
				if(line == null || "".equals(line)){
					break;
				}
				lines.add(line);
			}
		} finally {
			reader.close();
		}

		return lines;
	}
}