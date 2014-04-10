package org.qsardb.resolution.doi;

import java.io.*;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jbibtex.*;

public class DOIResolver {

	public static String asBibTeX(String doi) throws IOException {
		BibTeXDatabase db = resolveDOI(doi);

		BibTeXFormatter formatter = new BibTeXFormatter();
		Writer out = new StringWriter();
		formatter.format(db, out);
		return out.toString();
	}

	private static BibTeXDatabase resolveDOI(String doi) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();

		try {
			HttpGet request = new HttpGet("http://dx.doi.org/" + doi);
			request.addHeader("Accept", "text/bibliography; style=bibtex");

			CloseableHttpResponse response = client.execute(request);
			
			try {
				StatusLine status = response.getStatusLine();
				if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					throw new FileNotFoundException(doi);
				} else if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new IOException(status.getReasonPhrase());
				}
				
				return parseResponse(response);
			} finally {
				response.close();
			}
		} finally {
			client.close();
		}

	}

	private static BibTeXDatabase parseResponse(CloseableHttpResponse response) throws IOException {
		InputStream is = response.getEntity().getContent();
		BibTeXParser parser = new BibTeXParser();

		BibTeXDatabase db;
		try {
			return parser.parse(new InputStreamReader(is, "UTF-8"));
		} catch (TokenMgrError ex) {
			throw new IOException(ex.getMessage());
		} catch (ParseException ex) {
			throw new IOException(ex.getMessage());
		}
	}

}
