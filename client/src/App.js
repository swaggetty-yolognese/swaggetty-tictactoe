import React, { Component } from 'react';

import Grid from '@material-ui/core/Grid';

import Navigation from './components/Navigation'
import Layout from './containers/Layout'

class App extends Component {
  render() {
    return (
      <Layout>
        <Navigation />

        <Grid container spacing={16} justify="center">
          <Grid item xs={12} sm={8}>
            henlo
          </Grid>

          <Grid item xs={12} sm={4}>
            hola
          </Grid>
        </Grid>
      </Layout>
    );
  }
}

export default App;
