import React, { Component } from "react";
import "./Pagination.css";

/*
 * Many parts are copied from defualt pagination component in order to provide expected behaivior.
 * See: https://github.com/tannerlinsley/react-table/blob/v6/src/pagination.js
 */
class Pagination extends Component {
  static defaultProps = {
    extraData: undefined
  };

  constructor(props) {
    super(props);

    this.getSafePage = this.getSafePage.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
    this.applyPage = this.applyPage.bind(this);
    this.onPageChange = this.onPageChange.bind(this);
    this.changePage = this.changePage.bind(this);
    this.onPageSizeChange = this.onPageSizeChange.bind(this);

    this.state = {
      page: props.page
    };
  }

  componentDidUpdate(prevProps, prevState) {
    if (
      prevProps.page !== this.props.page &&
      prevState.page !== this.state.page
    ) {
      // this is probably safe because we only update when old/new state.page are different
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        page: this.props.page
      });
    }
  }

  getSafePage(page) {
    if (Number.isNaN(page)) {
      page = this.props.page;
    }
    return Math.min(Math.max(page, 0), this.props.pages - 1);
  }

  applyPage(e) {
    if (e) {
      e.preventDefault();
    }
    const page = this.state.page;
    this.changePage(page === "" ? this.props.page : page);
  }

  onKeyPress(e) {
    if (e.which === 13 || e.keyCode === 13) {
      this.applyPage();
    }
  }

  onPageChange(e) {
    const val = e.target.value;
    const page = val - 1;

    if (val === "") {
      return this.setState({ page: val });
    }

    this.setState({ page: this.getSafePage(page) });
  }

  changePage(page) {
    page = this.getSafePage(page);
    this.setState({ page });
    if (this.props.page !== page) {
      this.props.onPageChange(page);
    }
  }

  onPageSizeChange(e) {
    const { onPageSizeChange } = this.props;
    const value = parseInt(e.target.value, 10);
    // we need to reset page in the state in order to get correct new page, however we need
    // to do this for all pages except first one
    if (this.state.page !== 0) {
      this.setState({ page: -1 });
    }
    onPageSizeChange(value);
  }

  render() {
    const {
      extraData,
      page,
      canPrevious,
      canNext,
      pageSize,
      pageSizeOptions,
      pages,
      rowsSelectorText,
      rowsText,
      pageJumpText
    } = this.props;

    return (
      <div className="Pagination">
        <div className="Pagination-left">{extraData}</div>
        <div className="Pagination-center">
          <button
            type="button"
            className="Pagination-button Pagination-buttonPrev"
            onClick={() => this.changePage(page - 1)}
            disabled={!canPrevious}
          >
            <img src="/images/arrow-left.png" alt="" />
            Previous
          </button>
          <button
            type="button"
            className="Pagination-button Pagination-buttonNext"
            onClick={() => this.changePage(page + 1)}
            disabled={!canNext}
          >
            Next
            <img src="/images/arrow-right.png" alt="" />
          </button>
        </div>
        <div className="Pagination-right">
          <div className="Pagination-pages">
            <span>Page&nbsp;</span>
            <input
              aria-label={pageJumpText}
              type="number"
              className="Pagination-input Pagination-pageSelector"
              value={this.state.page === "" ? "" : this.state.page + 1}
              onChange={this.onPageChange}
              onBlur={this.applyPage}
              onKeyPress={this.onKeyPress}
              min="1"
              max={pages}
            />
            <span>{` of ${pages}`}</span>
          </div>
          <select
            aria-label={rowsSelectorText}
            className="Pagination-input Pagination-pageSize"
            value={pageSize}
            onChange={this.onPageSizeChange}
          >
            {pageSizeOptions.map((option, i) => (
              <option key={i} value={option}>{`${option} ${rowsText}`}</option>
            ))}
          </select>
        </div>
      </div>
    );
  }
}

export default Pagination;
