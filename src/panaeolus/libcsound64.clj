(ns panaeolus.libcsound64
  (:require [clojure.java.io :as io]
            [cpath-clj.core :as cp]))

(def ^:private current-csound-version "6.13")

(defn- get-os
  "Return the OS as a keyword. One of :windows :linux :mac"
  []
  (let [os (System/getProperty "os.name")]
    (cond
      (re-find #"[Ww]indows" os) :windows
      (re-find #"[Ll]inux" os)   :linux
      (re-find #"[Mm]ac" os)     :mac)))

(defn- get-cache-dir []
  (let [os (get-os)
        home (System/getProperty "user.home")]
    (case os
      :windows (let [local-app-data (or (System/getenv "APPDATA")
                                        (.getAbsolutePath
                                         (io/file home "AppData" "Local")))]
                 (.getAbsolutePath (io/file local-app-data "panaeolus" "Cache")))
      :linux (or (System/getenv "XDG_CACHE_HOME")
                 (.getAbsolutePath (io/file home ".cache" "panaeolus")))
      :mac (let [library (.getAbsolutePath (io/file home "Library"))]
             (.getAbsolutePath
              (io/file library "Caches" "panaeolus"))))))

(def ^:private csound-cache-folder
  (io/file (get-cache-dir)
           (str "csound-" current-csound-version)))

(defn cache-csound!
  "Cache csound and return the cache directory"
  []
  (when (not (.exists csound-cache-folder))
    (.mkdirs csound-cache-folder))
  (let [os (get-os)
        classp-loc (io/file
                    "libcsound64"
                    (case os :linux "linux" :mac "darwin" :windows "windows")
                    "x86_64")
        resource-dir (cp/resources (.getPath classp-loc))
        cache-foler-location (.getAbsolutePath csound-cache-folder)]
    (doseq [[file-name path-obj] resource-dir]
      (let [destination (io/file (str cache-foler-location file-name))]
        (when-not (.exists destination)
          (with-open [in (io/input-stream (first path-obj))]
            (io/copy in destination)))))
    cache-foler-location))
