package com.database;

import com.model.DataResponse;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.IOException;
import java.util.Objects;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;


public class NosqlConnection implements Connection {
    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<DataResponse> collection;
    String sequenceName;

    public void establishConnection(String host) {
        ConnectionString connectionString = new ConnectionString("mongodb://" + host);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();

        try{
            mongoClient = MongoClients.create(clientSettings);
            db = mongoClient.getDatabase("MongoDb");
        } catch (Exception e) {
            System.out.println("Failed to connect to the DB");
            e.printStackTrace();
        }
    }

    public void selectCollection(String collectionName) {
        collection = db.getCollection(collectionName, DataResponse.class);
        sequenceName = collectionName + "_sequence";
    }

    public int generateSequence(String seqName) {
        MongoOperations mongoOps = new MongoTemplate(mongoClient, "MongoDb");
        DatabaseSequence counter = mongoOps.findAndModify(new Query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq",1), options().returnNew(true).upsert(true),
                DatabaseSequence.class);
        return (int)(!Objects.isNull(counter) ? counter.getSeq() : 1);
    }

    public int createData(String someData) {
        int id = generateSequence(sequenceName);
        DataResponse newData = new DataResponse();
        newData.setId(id);
        newData.setSomeData(someData);
        newData.setStatus("IN_PROGRESS");
        collection.insertOne(newData);
        return id;
    }

    public int changeData(int id, String someData)  {

        DataResponse oldData;
        try {
            oldData = getData(id);
        } catch (IOException e) {
            return 0;
        }

        oldData.setSomeData(someData);
        oldData.setStatus("IN_PROGRESS");

        Document filterByDataId = new Document("_id", oldData.getId());
        FindOneAndReplaceOptions returnDocAfterReplace = new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER);
        DataResponse updatedData = collection.findOneAndReplace(filterByDataId, oldData, returnDocAfterReplace);
        System.out.println("Data replaced:\t" + updatedData);

        return 1;
    }

    public DataResponse getData(int id) throws IOException {
        DataResponse statusResponse = collection.find(Filters.eq("_id", id)).first();
        if( statusResponse == null){
            throw new IOException();
        }

        return statusResponse;
    }



    @Override
    public void changeStatus(int id, String status) throws IOException {
        DataResponse oldData = getData(id);
        oldData.setStatus(status);
        Document filterByDataId = new Document("_id", oldData.getId());
        FindOneAndReplaceOptions returnDocAfterReplace = new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER);
        collection.findOneAndReplace(filterByDataId, oldData, returnDocAfterReplace);
    }


}
