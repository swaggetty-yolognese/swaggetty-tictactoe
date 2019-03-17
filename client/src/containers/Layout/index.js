import React from 'react'

import { withStyles } from '@material-ui/core/styles';
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles';

const styles = {
  root: {
    flexGrow: 1,
    margin: '0px'
  },
};

class Layout extends React.Component {
  render() {
    const { classes } = this.props;

    const theme = createMuiTheme({
      palette: {
        primary: {
          main: '#2196f3',
          mainRgb: '33,150,243',
          darker: '#1769aa',
          lighter: '#4dabf5'
        },
        secondary: {
          main: '#e91e63',
          darker: '#a31545',
          lighter: '#ed4b82'
        },
        others: {
          lightGray: '#f5f5f5'
        }
      },
      status: {
        danger: 'orange',
      },
      typography: {
        useNextVariants: true,

        fontFamily: [
          'Heebo',
          'Roboto',
          '-apple-system',
          'BlinkMacSystemFont',
          '"Segoe UI"',
          '"Helvetica Neue"',
          'Arial',
          'sans-serif',
          '"Apple Color Emoji"',
          '"Segoe UI Emoji"',
          '"Segoe UI Symbol"',
        ].join(','),
      },
      paddings: {
        container: '52px 32px',
        containerTall: '92px 32px',
        containerTallY: '92px',
        slideSeparator: 52,
        block: 16
      },
      margins: {
        slideSeparator: 52,
        content: 16
      },
      buttons: {
        huge: {
          padding: '12px 40px',
          fontSize: 20
        }
      }
    });


    return(
      <MuiThemeProvider theme={theme}>
        <div className={classes.root}>
          { this.props.children }
        </div>
      </MuiThemeProvider>
    )
  }
}

export default withStyles(styles)(Layout);
