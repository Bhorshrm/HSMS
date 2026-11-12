SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @tableId int = OBJECT_ID(N'dbo.appointments', N'U');
    IF @tableId IS NULL
        THROW 50001, 'Table dbo.appointments was not found.', 1;

    DECLARE @indexName sysname;
    DECLARE @isConstraint bit;
    DECLARE doctorOnlyUniqueIndexes CURSOR LOCAL FAST_FORWARD FOR
        SELECT indexes.name, indexes.is_unique_constraint
        FROM sys.indexes AS indexes
        WHERE indexes.object_id = @tableId
          AND indexes.is_unique = 1
          AND indexes.is_primary_key = 0
          AND (
              SELECT COUNT(*)
              FROM sys.index_columns AS indexColumns
              WHERE indexColumns.object_id = indexes.object_id
                AND indexColumns.index_id = indexes.index_id
                AND indexColumns.is_included_column = 0
          ) = 1
          AND EXISTS (
              SELECT 1
              FROM sys.index_columns AS indexColumns
              JOIN sys.columns AS columns
                ON columns.object_id = indexColumns.object_id
               AND columns.column_id = indexColumns.column_id
              WHERE indexColumns.object_id = indexes.object_id
                AND indexColumns.index_id = indexes.index_id
                AND indexColumns.is_included_column = 0
                AND columns.name = N'doctor_id'
          );

    OPEN doctorOnlyUniqueIndexes;
    FETCH NEXT FROM doctorOnlyUniqueIndexes INTO @indexName, @isConstraint;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        IF @isConstraint = 1
            EXEC(N'ALTER TABLE dbo.appointments DROP CONSTRAINT ' + QUOTENAME(@indexName));
        ELSE
            EXEC(N'DROP INDEX ' + QUOTENAME(@indexName) + N' ON dbo.appointments');

        FETCH NEXT FROM doctorOnlyUniqueIndexes INTO @indexName, @isConstraint;
    END;

    CLOSE doctorOnlyUniqueIndexes;
    DEALLOCATE doctorOnlyUniqueIndexes;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes AS indexes
        WHERE indexes.object_id = @tableId
          AND indexes.is_unique = 1
          AND (
              SELECT COUNT(*)
              FROM sys.index_columns AS indexColumns
              WHERE indexColumns.object_id = indexes.object_id
                AND indexColumns.index_id = indexes.index_id
                AND indexColumns.is_included_column = 0
          ) = 2
          AND EXISTS (
              SELECT 1
              FROM sys.index_columns AS indexColumns
              JOIN sys.columns AS columns
                ON columns.object_id = indexColumns.object_id
               AND columns.column_id = indexColumns.column_id
              WHERE indexColumns.object_id = indexes.object_id
                AND indexColumns.index_id = indexes.index_id
                AND indexColumns.is_included_column = 0
                AND indexColumns.key_ordinal = 1
                AND columns.name = N'doctor_id'
          )
          AND EXISTS (
              SELECT 1
              FROM sys.index_columns AS indexColumns
              JOIN sys.columns AS columns
                ON columns.object_id = indexColumns.object_id
               AND columns.column_id = indexColumns.column_id
              WHERE indexColumns.object_id = indexes.object_id
                AND indexColumns.index_id = indexes.index_id
                AND indexColumns.is_included_column = 0
                AND indexColumns.key_ordinal = 2
                AND columns.name = N'appointment_date_time'
          )
    )
        ALTER TABLE dbo.appointments
            ADD CONSTRAINT UQ_appointments_doctor_id_appointment_date_time
            UNIQUE (doctor_id, appointment_date_time);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF CURSOR_STATUS('local', 'doctorOnlyUniqueIndexes') >= 0
        CLOSE doctorOnlyUniqueIndexes;
    IF CURSOR_STATUS('local', 'doctorOnlyUniqueIndexes') > -3
        DEALLOCATE doctorOnlyUniqueIndexes;
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
