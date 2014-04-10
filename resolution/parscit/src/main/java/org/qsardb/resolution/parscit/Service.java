/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.resolution.parscit;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jbibtex.*;
import org.jbibtex.ParseException;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Service {

	private Service(){
	}

    static
	public BibTeXDatabase parse(String citation) throws IOException, ParseException {
		String html = resolve(citation);

		Document document = Jsoup.parse(html);

		Elements bibChildren = document.select("div[id = bib] > pre");
		if(bibChildren.size() != 1){
			throw new IllegalArgumentException(citation);
		}

		Element bibChild = bibChildren.first();

		StringReader reader = new StringReader(bibChild.text());

		try {
			BibTeXParser parser = new BibTeXParser();

			BibTeXDatabase database = parser.parse(reader);

			Map<Key, BibTeXEntry> entries = database.getEntries();
			if(entries.size() != 1){
				throw new IllegalArgumentException(citation);
			}

			return database;
		} finally {
			reader.close();
		}
	}

	static
	private String resolve(String citation) throws IOException {
		URL url = new URL("http://aye.comp.nus.edu.sg/parsCit/parsCit.cgi");

		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(10 * 1000)
				.setSocketTimeout(10 * 1000).build();
		CloseableHttpClient client = HttpClients.custom()
				.setDefaultRequestConfig(config).build();

		try {
			List<NameValuePair> form = new ArrayList<NameValuePair>();
			form.add(new BasicNameValuePair("demo", "3"));
			form.add(new BasicNameValuePair("textlines", citation));
			form.add(new BasicNameValuePair("bib3", "on"));
			HttpPost request = new HttpPost(url.toURI());
			request.setEntity(new UrlEncodedFormEntity(form, Consts.UTF_8));

			HttpResponse response = client.execute(request);

			StatusLine status = response.getStatusLine();

			switch(status.getStatusCode()){
				case HttpStatus.SC_OK:
					break;
				default:
					throw new IOException(status.getReasonPhrase());
			}

			ByteArrayOutputStream os = new ByteArrayOutputStream();

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
			client.close();
		}
	}
}