﻿namespace Raven.Documentation.Samples.Migration.Server
{
    public class Indexes
    {
        /*
        #region indexes_1
        from doc in doc.Orders
        let company = LoadDocument(doc.Company)
        select new 
        {
            CompanyName: company.Name
        }
        #endregion
         */

        /*
        #region indexes_2
        from doc in doc.Orders
        let company = LoadDocument(doc.Company, "Companies")
        select new 
        {
            CompanyName: company.Name
        }
        #endregion
         */

        /*
        #region indexes_3
        from b in docs.Places
        select new {
	        _ = AbstractIndexCreationTask.SpatialGenerate(
                                    (double?)b.Latitude, 
                                    (double?)b.Longitude)
        }
        #endregion
        */

        /*
        #region indexes_4
        from b in docs.Places
        select new {
            Coordinates = CreateSpatialField(
                                (double?)b.Latitude, 
                                (double?)b.Longitude)
        }
        #endregion
        */

        /*
        #region indexes_
        #endregion
        */

        /*
        #region indexes_
        #endregion
        */
    }
}
