package net.ravendb.ClientApi.Operations.Patches;

import net.ravendb.client.Parameters;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.commands.batches.ICommandData;
import net.ravendb.client.documents.commands.batches.PatchCommandData;
import net.ravendb.client.documents.operations.*;
import net.ravendb.client.documents.queries.IndexQuery;
import net.ravendb.client.documents.queries.QueryOperationOptions;
import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.client.documents.session.JavaScriptArray;
import net.ravendb.client.documents.session.SessionInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PatchRequests {


    private static class BlogComment {
        private String content;
        private String title;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }


    private interface IFoo {
        //region patch_generic_interface_increment
        <T, U> void increment(String id, String path, U valueToAdd);

        <T, U> void increment(T entity, String path, U valueToAdd);
        //endregion

        //region patch_generic_interface_set_value
        <T, U> void patch(String id, String path, U value);

        <T, U> void patch(T entity, String path, U value);
        //endregion

        //region patch_generic_interface_array_modification_lambda
        <T, U> void patch(T entity, String pathToArray, Consumer<JavaScriptArray<U>> arrayAdder);

        <T, U> void patch(String id, String pathToArray, Consumer<JavaScriptArray<U>> arrayAdder);
        //endregion

        //region patch_non_generic_interface_in_session
        void defer(ICommandData[] commands);
        //endregion

        //region patch_non_generic_interface_in_store
        PatchStatus send(PatchOperation operation);

        PatchStatus send(PatchOperation operation, SessionInfo sessionInfo);

        <TEntity> PatchOperation.Result<TEntity> send(Class<TEntity> entityClass, PatchOperation operation);

        <TEntity> PatchOperation.Result<TEntity> send(Class<TEntity> entityClass, PatchOperation operation, SessionInfo sessionInfo);
        //endregion
    }

    public PatchRequests() {
        try (IDocumentStore store = new DocumentStore()) {
            try (IDocumentSession session = store.openSession()) {
                //region patch_firstName_generic
                // change firstName to Robert
                session
                    .advanced()
                    .patch("employees/1", "firstName", "Robert");
                //endregion

                session.saveChanges();
            }

            try (IDocumentSession session = store.openSession()) {
                //region patch_firstName_non_generic_session
                // change firstName to Robert
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.firstName = args.firstName");
                patchRequest.setValues(Collections.singletonMap("firstName", "Robert"));
                PatchCommandData patchCommandData = new PatchCommandData("employees/1", null, patchRequest, null);
                session.advanced().defer(patchCommandData);

                session.saveChanges();
                //endregion
            }

            {
                //region patch_firstName_non_generic_store
                // change firstName to Robert
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.firstName = args.firstName;");
                patchRequest.setValues(Collections.singletonMap("firstName", "Robert"));
                PatchOperation patchOperation = new PatchOperation("employees/1", null, patchRequest);
                store.operations().send(patchOperation);
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region patch_firstName_and_lastName_generic
                // change firstName to Robert and lastName to Carter in single request
                // note that in this case, we create single request, but two separate batch operations
                // in order to achieve atomicity, please use the non generic APIs
                session.advanced().patch("employees/1", "firstName", "Robert");
                session.advanced().patch("employees/1", "lastName", "Carter");

                session.saveChanges();
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region pathc_firstName_and_lastName_non_generic_session
                // change firstName to Robert and lastName to Carter in single request
                // note that here we do maintain the atomicity of the operation
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.firstName = args.firstName;" +
                    "this.lastName = args.lastName");

                Map<String, Object> values = new HashMap<>();
                values.put("firstName", "Robert");
                values.put("lastName", "Carter");
                patchRequest.setValues(values);

                session.advanced().defer(new PatchCommandData("employees/1", null, patchRequest, null));
                session.saveChanges();
                //endregion
            }

            {
                //region pathc_firstName_and_lastName_store
                // change FirstName to Robert and LastName to Carter in single request
                // note that here we do maintain the atomicity of the operation
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.firstName = args.firstName; " +
                    "this.lastName = args.lastName");

                Map<String, Object> values = new HashMap<>();
                values.put("firstName", "Robert");
                values.put("lastName", "Carter");
                patchRequest.setValues(values);

                store.operations().send(new PatchOperation("employees/1", null, patchRequest));
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region increment_age_generic
                // increment UnitsInStock property value by 10
                session.advanced().increment("products/1-A", "unitsInStock", 10);

                session.saveChanges();
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region increment_age_non_generic_session
                PatchRequest request = new PatchRequest();
                request.setScript("this.unitsInStock += args.unitsToAdd");
                request.setValues(Collections.singletonMap("unitsToAdd", 10));

                session.advanced().defer(
                    new PatchCommandData("products/1-A", null, request, null));
                session.saveChanges();
                //endregion
            }

            {
                //region increment_age_non_generic_store
                PatchRequest request = new PatchRequest();
                request.setScript("this.unitsInStock += args.unitsToAdd");
                request.setValues(Collections.singletonMap("unitsToAdd", 10));
                store.operations().send(new PatchOperation("products/1-A", null, request));
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region remove_property_age_non_generic_session
                // remove property Age
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("delete this.age");
                session.advanced().defer(
                    new PatchCommandData("employees/1", null, patchRequest, null));
                session.saveChanges();
                //endregion
            }

            {
                //region remove_property_age_store
                //remove property age
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("delete this.age");
                store.operations().send(new PatchOperation("employees/1", null, patchRequest));
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region rename_property_age_non_generic_session
                // rename firstName to First

                Map<String, Object> value = new HashMap<>();
                value.put("old", "firstName");
                value.put("new", "name");

                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("var firstName = this[args.rename.old];" +
                    "delete this[args.rename.old];" +
                    "this[args.rename.new] = firstName;");
                patchRequest.setValues(Collections.singletonMap("rename", value));

                session.advanced().defer(new PatchCommandData("employees/1", null, patchRequest, null));

                session.saveChanges();
                //endregion
            }

            {
                //region rename_property_age_store
                Map<String, Object> value = new HashMap<>();
                value.put("old", "firstName");
                value.put("new", "name");

                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("var firstName = this[args.rename.old];" +
                    "delete this[args.rename.old];" +
                    "this[args.rename.new] = firstName;");
                patchRequest.setValues(Collections.singletonMap("rename", value));

                store.operations().send(new PatchOperation("employees/1", null, patchRequest));
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region add_new_comment_to_comments_generic_session
                BlogComment comment = new BlogComment();
                comment.setContent("Lore ipsum");
                comment.setTitle("Some title");

                session.advanced()
                    .patch("blgoposts/1", "comments", comments -> comments.add(comment));

                session.saveChanges();
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region add_new_comment_to_comments_non_generic_session
                // add a new comment to comments
                BlogComment comment = new BlogComment();
                comment.setContent("Lore ipsum");
                comment.setTitle("Some title");

                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.comments.push(args.comment");
                patchRequest.setValues(Collections.singletonMap("comment", comment));

                session.advanced().defer(new PatchCommandData("blogposts/1", null, patchRequest, null));
                session.saveChanges();
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region add_new_comment_to_comments_store
                // add a new comment to comments
                BlogComment comment = new BlogComment();
                comment.setContent("Lore ipsum");
                comment.setTitle("Some title");

                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.comments.push(args.comment");
                patchRequest.setValues(Collections.singletonMap("comment", comment));

                store.operations().send(new PatchOperation("blogposts/1", null, patchRequest));
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region insert_new_comment_at_position_1_session
                BlogComment comment = new BlogComment();
                comment.setContent("Lore ipsum");
                comment.setTitle("Some title");

                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.comments.splice(1, 0, args.comment)");
                patchRequest.setValues(Collections.singletonMap("comment", comment));

                session.advanced().defer(new PatchCommandData("blogposts/1", null, patchRequest, null));
                session.saveChanges();
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region insert_new_comment_at_position_1_store
                BlogComment comment = new BlogComment();
                comment.setContent("Lore ipsum");
                comment.setTitle("Some title");

                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.comments.splice(1, 0, args.comment)");
                patchRequest.setValues(Collections.singletonMap("comment", comment));

                store.operations().send(new PatchOperation("blogposts/1", null, patchRequest));
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region modify_a_comment_at_position_3_in_comments_session
                // modify a comment at position 3 in Comments
                BlogComment comment = new BlogComment();
                comment.setContent("Lore ipsum");
                comment.setTitle("Some title");

                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.comments.splice(3, 1, args.comment)");
                patchRequest.setValues(Collections.singletonMap("comment", comment));

                session.advanced().defer(new PatchCommandData("blogposts/1", null, patchRequest, null));
                session.saveChanges();
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region modify_a_comment_at_position_3_in_comments_store
                // modify a comment at position 3 in Comments
                BlogComment comment = new BlogComment();
                comment.setContent("Lore ipsum");
                comment.setTitle("Some title");

                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.comments.splice(3, 1, args.comment)");
                patchRequest.setValues(Collections.singletonMap("comment", comment));

                store.operations().send(new PatchOperation("blogposts/1", null, patchRequest));
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region filter_items_from_array_session
                //filter out all comments of a blogpost which contains the word "wrong" in their contents
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.comments = this.comments.filter(comment " +
                    "=> comment.content.includes(args.titleToRemove));");
                patchRequest.setValues(Collections.singletonMap("titleToRemove", "wrong"));

                session.advanced().defer(
                    new PatchCommandData("blogposts/1", null, patchRequest, null));
                session.saveChanges();
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region filter_items_from_array_store
                // filter out all comments of a blogpost which contains the word "wrong" in their contents
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.comments = this.comments.filter(comment " +
                    "=> comment.content.includes(args.titleToRemove));");
                patchRequest.setValues(Collections.singletonMap("titleToRemove", "wrong"));

                store.operations().send(new PatchOperation("blogposts/1", null, patchRequest));
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region update_product_name_in_order_session
                // update product names in order, according to loaded product documents
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.lines.forEach(line => {" +
                    " var productDoc = load(line.product);" +
                    " line.productName = productDoc.name;" +
                    "});");

                session.advanced().defer(
                    new PatchCommandData("orders/1", null, patchRequest, null));
                session.saveChanges();
                //endregion
            }

            try (IDocumentSession session = store.openSession()) {
                //region update_product_name_in_order_store
                // update product names in order, according to loaded product documents
                PatchRequest patchRequest = new PatchRequest();
                patchRequest.setScript("this.lines.forEach(line => {" +
                    " var productDoc = load(line.product);" +
                    " line.productName = productDoc.name;" +
                    "});");

                store.operations().send(new PatchOperation("blogposts/1", null, patchRequest));
                //endregion
            }
        }

        try (IDocumentStore store = new DocumentStore()) {
            //region update_value_in_whole_collection
            // increase by 10 Freight field in all orders
            Operation operation = store
                .operations()
                .sendAsync(new PatchByQueryOperation("from Orders as o update  {" +
                    "   o.freight += 10;" +
                    "}"));

            // Wait for the operation to be complete on the server side.
            // Not waiting for completion will not harm the patch process and it will continue running to completion.
            operation.waitForCompletion();

            //endregion
        }

        try (IDocumentStore store = new DocumentStore()) {
            //region update-value-by-dynamic-query
            Operation operation = store
                .operations()
                .sendAsync(new PatchByQueryOperation("from Orders as o" +
                    " where o.employee = 'employees/1-A'" +
                    " update " +
                    "{ " +
                    "  o.lines.forEach(line => line.discount = 0.3);" +
                    "}"));

            operation.waitForCompletion();
            //endregion
        }

        try (IDocumentStore store = new DocumentStore()) {
            //region update-value-by-index-query
            // switch all products with supplier 'suppliers/12-A' with 'suppliers/13-A'
            Operation operation = store
                .operations()
                .sendAsync(new PatchByQueryOperation(new IndexQuery("" +
                    "from index 'Product/Search' as p " +
                    " where p.supplier = 'suppliers/12-A'" +
                    " update {" +
                    "  p.supplier = 'suppliers/13-A'" +
                    "}")));


            operation.waitForCompletion();
            //endregion
        }

        try (IDocumentStore store = new DocumentStore()) {
            //region update-on-stale-results
            // patch on stale results

            QueryOperationOptions options = new QueryOperationOptions();
            options.setAllowStale(true);

            Operation operation = store
                .operations()
                .sendAsync(new PatchByQueryOperation(new IndexQuery(
                    "from Orders as o " +
                        "where o.company = 'companies/12-A' " +
                        "update " +
                        "{ " +
                        "    o.company = 'companies/13-A';" +
                        "} "
                ), options));


            operation.waitForCompletion();
            //endregion
        }

        try (IDocumentStore store = new DocumentStore()) {
            //region change-collection-name
            // delete the document before recreating it with a different collection name

            Operation operation = store
                .operations()
                .sendAsync(new PatchByQueryOperation(new IndexQuery(
                    "from Orders as c " +
                        "update {" +
                        " del(id(c));" +
                        " this['@metadata']['collection'] = 'New_Orders'; " +
                        " put(id(c), this); " +
                        "}"
                )));

            operation.waitForCompletion();
            //endregion
        }

        try (IDocumentStore store = new DocumentStore()) {
            //region change-all-documents
            // perform a patch on all documents using @all_docs keyword

            Operation operation = store
                .operations()
                .sendAsync(new PatchByQueryOperation(new IndexQuery(
                    "from @all_docs " +
                        " update " +
                        "{ " +
                        "  this.updated = true;" +
                        "}"
                )));

            operation.waitForCompletion();
            //endregion
        }

        try (IDocumentStore store = new DocumentStore()) {
            //region patch-by-id
            // perform a patch by document ID

            Operation operation = store
                .operations()
                .sendAsync(new PatchByQueryOperation(new IndexQuery(
                    "from @all_docs as d " +
                        " where id() in ('orders/1-A', 'companies/1-A')" +
                        " update " +
                        "{" +
                        "  d.updated = true; " +
                        "} "
                )));

            operation.waitForCompletion();
            //endregion
        }

        try (IDocumentStore store = new DocumentStore()) {
            //region patch-by-id-using-parameters
            // perform a patch by document ID
            IndexQuery indexQuery = new IndexQuery(
                "from @all_docs as d " +
                    " where id() in ($ids)" +
                    " update " +
                    " {" +
                    "    d.updated = true; " +
                    "} "
            );
            Parameters parameters = new Parameters();
            parameters.put("ids", new String[]{"orders/1-A", "companies/1-A"});
            indexQuery.setQueryParameters(parameters);
            Operation operation = store
                .operations()
                .sendAsync(new PatchByQueryOperation(indexQuery));

            operation.waitForCompletion();
            //endregion
        }

    }
}
