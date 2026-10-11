import React from 'react';

export default function Pagination({ 
  currentPage, 
  totalPages, 
  totalItems, 
  pageSize, 
  hasNext, 
  hasPrevious, 
  onPageChange 
}) {
  
  const handlePrevious = () => {
    if (hasPrevious) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (hasNext) {
      onPageChange(currentPage + 1);
    }
  };

  const handlePageClick = (pageNumber) => {
    onPageChange(pageNumber);
  };

  // Generate page numbers to display
  const getPageNumbers = () => {
    const pages = [];
    const maxPagesToShow = 5;
    
    let startPage = Math.max(0, currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);
    
    // Adjust start if we're near the end
    if (endPage - startPage < maxPagesToShow - 1) {
      startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    
    return pages;
  };

  const startItem = currentPage * pageSize + 1;
  const endItem = Math.min((currentPage + 1) * pageSize, totalItems);

  const pages = getPageNumbers();
  const firstVisible = pages[0];
  const lastVisible = pages[pages.length - 1];

  const navBtn =
    'relative inline-flex items-center justify-center h-8 min-w-8 px-2.5 text-body-sm font-medium ' +
    'border border-ink-200 transition-colors duration-150 cursor-pointer ' +
    'focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20';

  const navBtnIdle = 'bg-white text-ink-700 hover:bg-ink-50 hover:border-ink-300';
  const navBtnActive = 'bg-navy-800 border-navy-800 text-white';
  const navBtnDisabled = 'bg-ink-50 text-ink-400 cursor-not-allowed';

  return (
    <div className="bg-white px-4 py-3 sm:px-6 flex flex-wrap items-center justify-between gap-3 border-t border-ink-200">
      {/* Results info — wraps cleanly instead of squeezing the controls */}
      <p className="ds-caption tabular-nums flex-1 min-w-0">
        Showing <span className="font-semibold text-ink-700">{startItem}</span> to{' '}
        <span className="font-semibold text-ink-700">{endItem}</span> of{' '}
        <span className="font-semibold text-ink-700">{totalItems}</span> results
      </p>

      {/* Mobile: previous / next only */}
      <div className="sm:hidden flex items-center gap-2">
        <button
          type="button"
          onClick={handlePrevious}
          disabled={!hasPrevious}
          className="ds-btn ds-btn-sm ds-btn-secondary"
        >
          Previous
        </button>
        <button
          type="button"
          onClick={handleNext}
          disabled={!hasNext}
          className="ds-btn ds-btn-sm ds-btn-secondary"
        >
          Next
        </button>
      </div>

      {/* Desktop pagination */}
      <nav
        className="hidden sm:inline-flex items-center rounded-md overflow-hidden border border-ink-200"
        aria-label="Pagination"
      >
        {/* Previous */}
        <button
          type="button"
          onClick={handlePrevious}
          disabled={!hasPrevious}
          className={`${navBtn} rounded-l-md ${hasPrevious ? navBtnIdle : navBtnDisabled} border-r-0`}
        >
          <span className="sr-only">Previous</span>
          <i className="fas fa-chevron-left text-xs"></i>
        </button>

        {/* First page */}
        {firstVisible > 0 && (
          <>
            <button
              type="button"
              onClick={() => handlePageClick(0)}
              className={`${navBtn} ${navBtnIdle} border-r-0 tabular-nums`}
            >
              1
            </button>
            {firstVisible > 1 && (
              <span className={`${navBtn} ${navBtnIdle} border-r-0 text-ink-400`} aria-hidden="true">
                &hellip;
              </span>
            )}
          </>
        )}

        {/* Page numbers */}
        {pages.map((pageNum) => (
          <button
            key={pageNum}
            type="button"
            onClick={() => handlePageClick(pageNum)}
            aria-current={pageNum === currentPage ? "page" : undefined}
            className={`${navBtn} border-r-0 tabular-nums ${
              pageNum === currentPage ? navBtnActive : navBtnIdle
            }`}
          >
            {pageNum + 1}
          </button>
        ))}

        {/* Last page */}
        {lastVisible < totalPages - 1 && (
          <>
            {lastVisible < totalPages - 2 && (
              <span className={`${navBtn} ${navBtnIdle} border-r-0 text-ink-400`} aria-hidden="true">
                &hellip;
              </span>
            )}
            <button
              type="button"
              onClick={() => handlePageClick(totalPages - 1)}
              className={`${navBtn} ${navBtnIdle} border-r-0 tabular-nums`}
            >
              {totalPages}
            </button>
          </>
        )}

        {/* Next */}
        <button
          type="button"
          onClick={handleNext}
          disabled={!hasNext}
          className={`${navBtn} rounded-r-md ${hasNext ? navBtnIdle : navBtnDisabled}`}
        >
          <span className="sr-only">Next</span>
          <i className="fas fa-chevron-right text-xs"></i>
        </button>
      </nav>
    </div>
  );
}
