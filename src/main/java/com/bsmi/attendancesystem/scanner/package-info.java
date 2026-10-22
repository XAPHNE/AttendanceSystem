/**
 * Lets a phone act as a card reader: a small http server on the local network, guarded by a
 * random token, that serves a page for photographing a student's QR card or typing its ID, and
 * passes each ID to {@link com.bsmi.attendancesystem.service.AttendanceService#scan}. It runs
 * only while "Use phone as scanner" is switched on.
 */
package com.bsmi.attendancesystem.scanner;
