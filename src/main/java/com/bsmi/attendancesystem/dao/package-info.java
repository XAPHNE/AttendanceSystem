/**
 * Data access objects: the only code that talks SQL. Every query is a {@code PreparedStatement} in
 * try-with-resources, and any {@code SQLException} is wrapped in an unchecked {@link
 * com.bsmi.attendancesystem.dao.DataAccessException} naming the operation that failed.
 */
package com.bsmi.attendancesystem.dao;
