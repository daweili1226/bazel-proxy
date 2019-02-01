// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


/* add by kevin when work under proxy */
package com.google.devtools.build.lib.bazel.repository.downloader;
import java.net.URL;
import java.util.List;
import java.io.IOException;
import com.google.devtools.build.lib.vfs.Path;
import com.google.devtools.build.lib.bazel.repository.cache.RepositoryCache;
import com.google.devtools.build.lib.bazel.repository.cache.RepositoryCache.KeyType;

public class SimpleWget {
	public static final String DEFAULT_WGET = "wget";
	public static final String DEFAULT_WGET_OPT = "--tries=3 --timeout=15 --no-check-certificate";
	public static String wgetApp = null;
	public static String wgetOpt = null;

	public static void download(List<URL> urls, Path destination, String sha256) throws IOException {
		for (URL url : urls) {
			String command = WgetCommand(url.toString(), destination.toString());
			try {
				SimpleShell(command);
				if (FileValid(destination, sha256)) {
					System.out.println("wget download " + url.toString() + " OK");
					return;
				}
			}
			catch (Exception e) {
				System.out.println("WARN: error exec " + command + " [ " + e.getMessage() + " ]");
			}
		}
		throw new IOException("Wget error: " + urls + " to " + destination);
	}
	
	private static String WhichWget() {
		if (wgetApp == null) {
			wgetApp = System.getenv("BAZEL_WGET");
			if (wgetApp == null) {
				wgetApp = DEFAULT_WGET;
			}
		}
		return wgetApp;
	}

	private static String WgetOpts() {
		if (wgetOpt == null) {
			wgetOpt = System.getenv("BAZEL_WGET_OPT");
			if (wgetOpt == null) {
				wgetOpt = DEFAULT_WGET_OPT;
			}
		}
		return wgetOpt;
	}

	private static String WgetCommand(String url, String outFile) {
		String wget = WhichWget();
		String opts = WgetOpts();
		return wget + " " + opts + " " + url + " -O " + outFile;
	}

	public static boolean FileValid(Path destination, String sha256) {
		if (!destination.exists()) {
			return false;
		}
		if (!sha256.isEmpty()) {
			try {
				String currentSha256 =
					RepositoryCache.getChecksum(KeyType.SHA256, destination);
        		if (currentSha256.equals(sha256)) {
          			return true;
        		}
			} 
			catch (IOException e) {
				//ignore
			}
			return false;
		}
		return true;
    }

	private static void SimpleShell(String command) throws Exception {
		Process proc = Runtime.getRuntime().exec(command);
		proc.waitFor();
	}

}
