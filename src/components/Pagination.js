import React from 'react';

const COUNT_PER_PAGE = 6;

const Pagination = ({ currentPage, setCurrentPage, totalItems }) => {
    const getTotalPageCount = () => {
        return Math.ceil(totalItems.length / COUNT_PER_PAGE);
    };

    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    const renderPageButtons = () => {
        const pageButtons = [];
        for (let i = 1; i <= getTotalPageCount(); i++) {
            pageButtons.push(
                <span
                    key={i}
                    className={`number-button ${currentPage === i ? 'selected' : ''}`}
                    onClick={() => handlePageChange(i)}
                >
                    {i}
                </span>
            );
        }
        return pageButtons;
    };

    return (
        <div className="pagination-container">
            <div className="prev-button" onClick={() => handlePageChange(currentPage - 1)}>이전</div>
            <div className="number-button-wrapper">
                {renderPageButtons()}
            </div>
            <div className="next-button" onClick={() => handlePageChange(currentPage + 1)}>다음</div>
        </div>
    );
};

export default Pagination;
