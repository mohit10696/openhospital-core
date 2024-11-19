/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.exa.service;

import java.util.List;
import java.util.Objects;

import org.isf.exa.model.Exam;
import org.isf.exa.model.ExamRow;
import org.isf.exatype.model.ExamType;
import org.isf.exatype.service.ExamTypeIoOperationRepository;
import org.isf.utils.db.TranslateOHServiceException;
import org.isf.utils.exception.OHServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = OHServiceException.class)
@TranslateOHServiceException
public class ExamIoOperations extends CommonExamIOOperations {

	private final ExamIoOperationRepository repository;

	private final ExamRowIoOperationRepository rowRepository;

	private final ExamTypeIoOperationRepository typeRepository;

	public ExamIoOperations(ExamIoOperationRepository examIoOperationRepository, ExamRowIoOperationRepository examRowIoOperationRepository,
		ExamTypeIoOperationRepository examTypeIoOperationRepository) {
		this.repository = examIoOperationRepository;
		this.rowRepository = examRowIoOperationRepository;
		this.typeRepository = examTypeIoOperationRepository;
	}

	/**
	 * Returns the list of {@link Exam}s
	 * @return the list of {@link Exam}s
	 * @throws OHServiceException
	 */
	public List<Exam> getExams() throws OHServiceException {
		return getExamsByDesc(null);
	}

	/**
	 * Returns the list of {@link Exam}s that matches passed description
	 * @param description - the exam description
	 * @return the list of {@link Exam}s
	 * @throws OHServiceException
	 */
	public List<Exam> getExamsByDesc(String description) throws OHServiceException {
		return description != null ? repository.findByDescriptionContainingOrderByExamtypeDescriptionAscDescriptionAsc(description) :
			repository.findByOrderByDescriptionAscDescriptionAsc();
	}

	/**
	 * Returns the list of {@link Exam}s by {@link ExamType} description
	 * @param description - the exam description
	 * @return the list of {@link Exam}s
	 * @throws OHServiceException
	 */
	public List<Exam> getExamsByExamTypeDesc(String description) throws OHServiceException {
		return description != null ? repository.findByExamtype_DescriptionContainingOrderByExamtypeDescriptionAscDescriptionAsc(description) :
			repository.findByOrderByDescriptionAscDescriptionAsc();
	}


	/**
	 * Insert a new {@link Exam} with exam rows.
	 * @param payload - the {@link Exam} to insert
	 * @param rows - the {@link List<String>} to associate as exam rows
	 * @return the newly persisted {@link Exam}.
	 * @throws OHServiceException
	 */
	@Transactional
	public Exam create(Exam payload, List<String> rows) throws OHServiceException {
		Exam exam = repository.save(payload);
		if (exam.getProcedure() == 3) {
			return exam;
		}
		if (rows != null && !rows.isEmpty()) {
			rowRepository.saveAll(rows.stream().map(description -> new ExamRow(exam, description)).toList());
		}
		return exam;
	}

	/**
	 * Update an existing {@link Exam} with exam rows.
	 * @param payload - the {@link Exam} to insert
	 * @param rows - the {@link List<String>} to associate as exam rows
	 * @return the newly persisted {@link Exam}.
	 * @throws OHServiceException
	 */
	@Transactional
	public Exam update(Exam payload, List<String> rows) throws OHServiceException {
		Exam oldExam = findByCode(payload.getCode());
		Exam exam = repository.save(payload);
		List<ExamRow> examRows = rowRepository.findAllByExam_CodeOrderByDescription(exam.getCode());
		if (exam.getProcedure() == 3 && oldExam.getProcedure() != 3) {
			rowRepository.deleteAll(examRows);
		} else {
			List<ExamRow> rowsToRemove = examRows.stream().filter(examRow -> !rows.contains(examRow.getDescription())).toList();
			List<ExamRow> rowsToAdd = rows.stream().filter(row -> examRows.stream().noneMatch(examRow -> Objects.equals(row, examRow.getDescription())))
				.map(description -> new ExamRow(exam, description)).toList();

			if (!rowsToRemove.isEmpty()) {
				rowRepository.deleteAll(rowsToRemove);
			}
			if (!rowsToAdd.isEmpty()) {
				rowRepository.saveAll(rowsToAdd);
			}
		}
		return exam;
	}

	/**
	 * Insert a new {@link Exam}.
	 * @param exam - the {@link Exam} to insert
	 * @return the newly persisted {@link Exam}.
	 * @throws OHServiceException
	 */
	public Exam newExam(Exam exam) throws OHServiceException {
		return repository.save(exam);
	}

	/**
	 * Insert a new {@link ExamRow}.
	 * @param examRow - the {@link ExamRow} to insert
	 * @return the newly persisted {@link ExamRow}.
	 * @throws OHServiceException
	 */
	public ExamRow newExamRow(ExamRow examRow) throws OHServiceException {
		return rowRepository.save(examRow);
	}

	/**
	 * Update an already existing {@link Exam}.
	 * @param exam - the {@link Exam} to update
	 * @return the updated {@link Exam}.
	 * @throws OHServiceException
	 */
	public Exam updateExam(Exam exam) throws OHServiceException {
		return repository.save(exam);
	}

	/**
	 * Delete an {@link Exam}
	 * @param exam - the {@link Exam} to delete
	 * @throws OHServiceException
	 */
	public void deleteExam(Exam exam) throws OHServiceException {
		rowRepository.deleteByExam_Code(exam.getCode());
		repository.delete(exam);
	}

	/**
	 * This function controls the presence of a record with the same key as in the parameter; Returns false if the query finds no record, else returns true
	 * @param exam the {@link Exam}
	 * @return {@code true} if the Exam code has already been used, {@code false} otherwise
	 * @throws OHServiceException
	 */
	public boolean isKeyPresent(Exam exam) throws OHServiceException {
		return repository.findById(exam.getCode()).orElse(null) != null;
	}

	/**
	 * Sanitize the given {@link String} value. This method is maintained only for backward compatibility.
	 * @param value the value to sanitize.
	 * @return the sanitized value or {@code null} if the passed value is {@code null}.
	 */
	protected String sanitize(String value) {
		if (value == null) {
			return null;
		}
		return value.trim().replace("'", "''");
	}

	/**
	 * Checks if the code is already in use
	 * @param code - the exam code
	 * @return {@code true} if the code is already in use, {@code false} otherwise
	 * @throws OHServiceException
	 */
	public boolean isCodePresent(String code) throws OHServiceException {
		return repository.existsById(code);
	}


	/**
	 * Find exam by code
	 * @param code - the code
	 * @return The exam if found, {@code null} otherwise.
	 * @throws OHServiceException
	 */
	public Exam findByCode(String code) throws OHServiceException {
		return repository.findById(code).orElse(null);
	}
}