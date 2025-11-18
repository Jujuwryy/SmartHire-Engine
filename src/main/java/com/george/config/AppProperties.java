package com.george.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private Mongodb mongodb = new Mongodb();
    private Embeddings embeddings = new Embeddings();
    private Matching matching = new Matching();
    private Cache cache = new Cache();
    private Api api = new Api();
    
    public Mongodb getMongodb() {
        return mongodb;
    }
    
    public void setMongodb(Mongodb mongodb) {
        this.mongodb = mongodb;
    }
    
    public Embeddings getEmbeddings() {
        return embeddings;
    }
    
    public void setEmbeddings(Embeddings embeddings) {
        this.embeddings = embeddings;
    }
    
    public Matching getMatching() {
        return matching;
    }
    
    public void setMatching(Matching matching) {
        this.matching = matching;
    }
    
    public Cache getCache() {
        return cache;
    }
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
    
    public static class Mongodb {
        private String databaseName;
        private String collectionName;
        private String vectorIndexName;
        
        public String getDatabaseName() {
            return databaseName;
        }
        
        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }
        
        public String getCollectionName() {
            return collectionName;
        }
        
        public void setCollectionName(String collectionName) {
            this.collectionName = collectionName;
        }
        
        public String getVectorIndexName() {
            return vectorIndexName;
        }
        
        public void setVectorIndexName(String vectorIndexName) {
            this.vectorIndexName = vectorIndexName;
        }
    }
    
    public static class Embeddings {
        private Huggingface huggingface = new Huggingface();
        
        public Huggingface getHuggingface() {
            return huggingface;
        }
        
        public void setHuggingface(Huggingface huggingface) {
            this.huggingface = huggingface;
        }
        
        public static class Huggingface {
            private String accessToken;
            private String modelId;
            private int timeoutSeconds;
            private int dimension;
            
            public String getAccessToken() {
                return accessToken;
            }
            
            public void setAccessToken(String accessToken) {
                this.accessToken = accessToken;
            }
            
            public String getModelId() {
                return modelId;
            }
            
            public void setModelId(String modelId) {
                this.modelId = modelId;
            }
            
            public int getTimeoutSeconds() {
                return timeoutSeconds;
            }
            
            public void setTimeoutSeconds(int timeoutSeconds) {
                this.timeoutSeconds = timeoutSeconds;
            }
            
            public int getDimension() {
                return dimension;
            }
            
            public void setDimension(int dimension) {
                this.dimension = dimension;
            }
        }
    }
    
    public static class Matching {
        private int defaultLimit;
        private int maxLimit;
        private int minLimit;
        private double defaultMinConfidence;
        private Thresholds thresholds = new Thresholds();
        
        public int getDefaultLimit() {
            return defaultLimit;
        }
        
        public void setDefaultLimit(int defaultLimit) {
            this.defaultLimit = defaultLimit;
        }
        
        public int getMaxLimit() {
            return maxLimit;
        }
        
        public void setMaxLimit(int maxLimit) {
            this.maxLimit = maxLimit;
        }
        
        public int getMinLimit() {
            return minLimit;
        }
        
        public void setMinLimit(int minLimit) {
            this.minLimit = minLimit;
        }
        
        public double getDefaultMinConfidence() {
            return defaultMinConfidence;
        }
        
        public void setDefaultMinConfidence(double defaultMinConfidence) {
            this.defaultMinConfidence = defaultMinConfidence;
        }
        
        public Thresholds getThresholds() {
            return thresholds;
        }
        
        public void setThresholds(Thresholds thresholds) {
            this.thresholds = thresholds;
        }
        
        public static class Thresholds {
            private double veryStrong;
            private double good;
            private double moderate;
            
            public double getVeryStrong() {
                return veryStrong;
            }
            
            public void setVeryStrong(double veryStrong) {
                this.veryStrong = veryStrong;
            }
            
            public double getGood() {
                return good;
            }
            
            public void setGood(double good) {
                this.good = good;
            }
            
            public double getModerate() {
                return moderate;
            }
            
            public void setModerate(double moderate) {
                this.moderate = moderate;
            }
        }
    }
    
    public static class Cache {
        private int embeddingTtlHours;
        private int queryTtlMinutes;
        
        public int getEmbeddingTtlHours() {
            return embeddingTtlHours;
        }
        
        public void setEmbeddingTtlHours(int embeddingTtlHours) {
            this.embeddingTtlHours = embeddingTtlHours;
        }
        
        public int getQueryTtlMinutes() {
            return queryTtlMinutes;
        }
        
        public void setQueryTtlMinutes(int queryTtlMinutes) {
            this.queryTtlMinutes = queryTtlMinutes;
        }
    }
    
    public static class Api {
        private String basePath;
        
        public String getBasePath() {
            return basePath;
        }
        
        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }
}

